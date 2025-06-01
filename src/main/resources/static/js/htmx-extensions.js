htmx.defineExtension('hx-dataset-include', {
    encodeParameters: function (xhr, parameters, elt) {
        Object
            .keys(elt.dataset)
            .forEach(k => parameters
                .append(k, elt.dataset[k]))
    }
})

document.addEventListener('htmx:responseError', evt => {

    const xhr = evt.detail.xhr;
    const alertContainer = document.getElementById('error-alert-container');

    if (alertContainer) {

        console.log("!!!")
        console.error(xhr.responseText);
        const alert =
            document.createElement('div');
        alert.className = "fixed top-3 transform left-1/2 -translate-x-1/2 z-50 flex justify-center transition-opacity duration-1000";
        alert.innerHTML = xhr.responseText;

        alertContainer.appendChild(alert);

        //alertContainer.

        setTimeout(() => {
            alert.classList.add('opacity-0');
            // fade-out 애니메이션 지속시간(예: 1초) 후 요소 제거
            setTimeout(() => {
                alert.remove();
            }, 50000);
        }, 3000);
    }

});

