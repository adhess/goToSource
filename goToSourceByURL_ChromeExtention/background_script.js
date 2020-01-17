chrome.browserAction.onClicked.addListener(goToComponent);
const NUMBER_PORT_SUPPORTED = 2;

function goToComponent() {
    chrome.tabs.query({active: true, currentWindow: true}, function (tabs) {
        var url = tabs[0].url;
        console.log(url);
        for (var i = 0; i < NUMBER_PORT_SUPPORTED; i++) {
            var xhr = new XMLHttpRequest();
            xhr.open('POST', 'http://localhost:' + (43872 + i) + '/goToComponent', true);
            xhr.setRequestHeader('Content-type', 'text/plain');
            xhr.send(url + '.');
        }
    })
}

var numberResponse = 0;

function getScreenRelatedComponent() {
    chrome.tabs.query({active: true, currentWindow: true}, function (tabs) {
        var url = tabs[0].url;
        console.log(url);
        numberResponse = 0;
        for (var i = 0; i < NUMBER_PORT_SUPPORTED; i++) {
            var xhr = new XMLHttpRequest();
            xhr.onreadystatechange = function () {
                numberResponse++;
                if (this.status === 200 && this.readyState === 4 && this.responseText) {
                    var responseText = this.responseText;
                    chrome.tabs.query({active: true, currentWindow: true}, function (tabs) {
                        chrome.tabs.sendMessage(tabs[0].id, {relatedComponents: responseText, target: 'selectors'}, function (response) {
                            console.log("Response: ", response);
                        });
                    });
                }
            };
            xhr.open('POST', 'http://localhost:' + (43872 + i) + '/getAllComponents', true);
            xhr.setRequestHeader('Content-type', 'text/plain');
            xhr.send(url + '.');
        }
    })
}

chrome.commands.onCommand.addListener(function (command) {
    if (command.toString() === 'goToComponent') {
        goToComponent();
    } else if (command.toString() === 'getAllComponents') {
        getScreenRelatedComponent()
    }
});

function f() {
    console.log("I m listening...");
}

f();

chrome.runtime.onMessage.addListener(function (msg, sender, sendResponse) {
    if (msg.target.toString() === 'goToComponentBySelector') {
        for (var i = 0; i < NUMBER_PORT_SUPPORTED; i++) {
            var xhr = new XMLHttpRequest();
            xhr.open('POST', 'http://localhost:' + (43872 + i) + '/goToComponentBySelector', true);
            xhr.setRequestHeader('Content-type', 'text/plain');
            xhr.send(msg.selector + '.');
        }
        alert(msg.selector);
    }
    sendResponse("200");
});