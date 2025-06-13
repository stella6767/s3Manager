// document.addEventListener("htmx:beforeSwap", (event) => {
//     const status = event.detail.xhr.status;
//     // 2xx가 아닌 경우 URL 변경 차단
//     if (status < 200 || status >= 300) {
//         console.log("status!!!", status)
//         event.detail.shouldPush = false;
//     }
// });

htmx.defineExtension('hx-dataset-include', {
    encodeParameters: function (xhr, parameters, elt) {
        Object
            .keys(elt.dataset)
            .forEach(k => parameters
                .append(k, elt.dataset[k]))
    }
})

function initUploadJS() {
    console.log('htmx 컨텐츠가 교체되었습니다. 이벤트 리스너를 다시 등록합니다.');
    initializeUploadPage();
}

