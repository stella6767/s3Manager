
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


