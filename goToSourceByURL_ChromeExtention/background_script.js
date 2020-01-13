chrome.browserAction.onClicked.addListener(sendURl);

function sendURl() {
    chrome.tabs.query({active: true, currentWindow: true}, function (tabs) {
        var url = tabs[0].url;
        console.log(url);
        for (var i = 1; i <= 15; i++) {
            var xhr = new XMLHttpRequest();
            xhr.open('POST', 'http://localhost:' + (43872 + i), true);
            xhr.setRequestHeader('Content-type', 'text/plain');
            xhr.send(url+'.');
        }

    })
}
function f() {
    console.log("I m listening...");
}

f();