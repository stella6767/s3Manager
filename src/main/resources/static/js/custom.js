

function moveFocus(e, el, nextId) {
    el.value = el.value.replace(/[^0-9]/g, '');
    if (el.value && nextId) {
        document.getElementById(nextId).focus();
    }
}

function collectAndSubmitCode(form) {
    const code = Array.from({ length: 6 }, (_, i) => {
        return document.getElementById(`code${i + 1}`).value;
    }).join('');

    // code가 6자리일 때만 submit
    if (code.length === 6) {
        document.getElementById('codeFull').value = code;
        form.dispatchEvent(new Event("verify-code", { bubbles: true }));
    }
}

function initInputField(){
    console.log("initInputField");
    document.querySelectorAll('#main-input-form input')
        .forEach(input => input.value = '');
}


document.addEventListener('htmx:responseError', evt => {
    const xhr = evt.detail.xhr;
    const alertContainer = document.getElementById('error-alert-container');

    console.log("????")

    if (alertContainer) {

        console.log("!!!")
        console.error(xhr.responseText);
        const alert =
            document.createElement('div');
        alert.className = "fixed top-3 left-6 right-6 z-50 flex justify-center transition-opacity duration-1000";
        alert.innerHTML = xhr.responseText;

        alertContainer.appendChild(alert);

        //alertContainer.

        setTimeout(() => {
            alert.classList.add('opacity-0');
            // fade-out 애니메이션 지속시간(예: 1초) 후 요소 제거
            setTimeout(() => {
                alert.remove();
            }, 1000);
        }, 3000);
    }

});


htmx.defineExtension('hx-dataset-include', {
    encodeParameters: function (xhr, parameters, elt) {
        Object
            .keys(elt.dataset)
            .forEach(k => parameters
                .append(k, elt.dataset[k]))
    }
})


