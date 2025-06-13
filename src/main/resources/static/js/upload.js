

document.addEventListener('DOMContentLoaded', () => {
    // 요소 가져오기
    const dropZone = document.getElementById('drop-zone');
    const fileInput = document.getElementById('file-input');
    const folderInput = document.getElementById('folder-input');
    const uploadBtn = document.getElementById('upload-btn');
    const clearBtn = document.getElementById('clear-btn');
    const fileListTbody = document.getElementById('file-list-tbody');
    const emptyRow = document.getElementById('empty-row');
    const fileCountEl = document.getElementById('file-count');
    const totalSizeEl = document.getElementById('total-size');

    let filesToUpload = [];

    // 이벤트 리스너 연결
    dropZone.addEventListener('click', (e) => {
        fileInput.click();
    });

    fileInput.addEventListener('change', (e) => handleFiles(e.target.files));
    folderInput.addEventListener('change', (e) => handleFiles(e.target.files));

    // 드래그 앤 드롭 이벤트
    dropZone.addEventListener('dragover', (e) => {
        e.preventDefault();
        dropZone.classList.add('border-primary');
    });
    dropZone.addEventListener('dragleave', (e) => {
        e.preventDefault();
        dropZone.classList.remove('border-primary');
    });
    dropZone.addEventListener('drop', (e) => {
        e.preventDefault();
        dropZone.classList.remove('border-primary');
        handleFiles(e.dataTransfer.files);
    });

    // 지우기 및 업로드 버튼
    clearBtn.addEventListener('click', () => {
        filesToUpload = [];
        updateFileList();
    });

    uploadBtn.addEventListener('click', () => {
        // TODO: 업로드 로직 시작
        console.log('업로드를 시작합니다:', filesToUpload);
        alert('업로드 기능은 여기에 구현해야 합니다.');
    });


    async function uploadAllFiles() {
        // 업로드 시작 시 버튼 비활성화
        uploadBtn.disabled = true;
        uploadBtn.textContent = '업로드 중...';

        const uploadPromises = filesToUpload.map((file, index) => {
            // 각 파일에 해당하는 테이블 행(tr)을 찾습니다.
            const tr = fileListTbody.rows[index];
            return uploadSingleFile(file, tr);
        });

        // 모든 파일 업로드가 끝날 때까지 기다립니다.
        await Promise.all(uploadPromises);

        alert('모든 파일 업로드가 완료되었습니다!');
        uploadBtn.textContent = '업로드';
        // 여기서 목록을 비우거나 다른 후속 조치를 할 수 있습니다.
        // filesToUpload = [];
        // updateFileList();
    }


    function uploadSingleFile(file, tr) {

        return new Promise(async (resolve, reject) => {
            const statusCell = tr.cells[4]; // 상태 <td>
            const progressBar = statusCell.querySelector('.progress');

            try {
                // 1. 서버에 사전 서명된 URL 요청
                statusCell.innerHTML = 'URL 요청 중...';
                const presignResponse = await fetch('/api/uploads/presigned-url', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ filename: file.name, contentType: file.type })
                });

                if (!presignResponse.ok) throw new Error('사전 서명된 URL을 받아오지 못했습니다.');

                const { url: presignedUrl } = await presignResponse.json();

                // 2. XMLHttpRequest를 사용하여 S3에 직접 업로드 (진행률 추적을 위해)
                const xhr = new XMLHttpRequest();
                xhr.open('PUT', presignedUrl, true);
                xhr.setRequestHeader('Content-Type', file.type);

                // 업로드 진행률 이벤트 리스너
                xhr.upload.onprogress = (event) => {
                    if (event.lengthComputable) {
                        const percentComplete = (event.loaded / event.total) * 100;
                        progressBar.value = percentComplete;
                        statusCell.innerHTML = `업로드 중: ${Math.round(percentComplete)}%`;
                        statusCell.prepend(progressBar); // progress 바를 다시 앞에 추가
                    }
                };

                // 업로드 완료 이벤트 리스너
                xhr.onload = () => {
                    if (xhr.status === 200) {
                        statusCell.innerHTML = '<span class="text-success font-semibold">완료</span>';
                        progressBar.value = 100;
                        resolve('Upload complete');
                    } else {
                        throw new Error(`업로드 실패: ${xhr.statusText}`);
                    }
                };

                // 업로드 에러 이벤트 리스너
                xhr.onerror = () => {
                    statusCell.innerHTML = '<span class="text-error font-semibold">실패</span>';
                    reject('Network error');
                };

                xhr.send(file);

            } catch (error) {
                console.error(error);
                statusCell.innerHTML = `<span class="text-error font-semibold">실패</span>`;
                reject(error);
            }
        });
    }


    // 파일 처리 함수
    function handleFiles(files) {
        for (const file of files) {
            // 중복 파일 체크 (선택사항)
            if (!filesToUpload.some(f => f.name === file.name && f.size === file.size)) {
                filesToUpload.push(file);
            }
        }
        updateFileList();
    }

    // 파일 목록 UI 업데이트 함수
    function updateFileList() {
        fileListTbody.innerHTML = ''; // 목록 비우기
        if (filesToUpload.length === 0) {
            fileListTbody.appendChild(emptyRow);
            uploadBtn.disabled = true;
        } else {
            uploadBtn.disabled = false;
            filesToUpload.forEach((file, index) => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <th><input type="checkbox" class="checkbox"></th>
                    <td>${file.name}</td>
                    <td>${file.type || '알 수 없음'}</td>
                    <td>${formatBytes(file.size)}</td>
                    <td>
                        <progress class="progress progress-secondary w-full" value="100" max="100"></progress>
                    </td>
                `;
                fileListTbody.appendChild(tr);
            });
        }
        // 파일 개수 및 총 크기 업데이트
        updateSummary();
    }

    function updateSummary() {
        fileCountEl.textContent = filesToUpload.length;
        const totalSize = filesToUpload.reduce((sum, file) => sum + file.size, 0);
        totalSizeEl.textContent = formatBytes(totalSize);
    }

    // 파일 크기 포맷팅 함수
    function formatBytes(bytes, decimals = 2) {
        if (bytes === 0) return '0 B';
        const k = 1024;
        const dm = decimals < 0 ? 0 : decimals;
        const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
    }
});
