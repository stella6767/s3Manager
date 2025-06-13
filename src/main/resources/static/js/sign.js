

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





