chrome.browserAction.onClicked.addListener(goToComponent);

function goToComponent() {
    chrome.tabs.query({active: true, currentWindow: true}, function (tabs) {
        var url = tabs[0].url;
        console.log(url);
        for (var i = 1; i <= 15; i++) {
            var xhr = new XMLHttpRequest();
            xhr.open('POST', 'http://localhost:' + (43872 + i) + '/goToComponent', true);
            xhr.setRequestHeader('Content-type', 'text/plain');
            xhr.send(url + '.');
        }
    })
}
function getAllComponents() {
    chrome.tabs.query({active: true, currentWindow: true}, function (tabs) {
        var url = tabs[0].url;
        console.log(url);
        for (var i = 1; i <= 15; i++) {
            var xhr = new XMLHttpRequest();
            xhr.onreadystatechange = function() {
                console.log(this.responseText);
            };
            xhr.open('POST', 'http://localhost:' + (43872 + i) + '/getAllComponents', true);
            xhr.setRequestHeader('Content-type', 'text/plain');
            xhr.send(url + '.');
        }
    })
}

chrome.commands.onCommand.addListener(function(command) {
    if (command.toString() === 'goToComponent') {
        goToComponent();
    } else if (command.toString() === 'getAllComponents') {
        getAllComponents()
    }
});
function f() {
    console.log("I m listening...");
}

f();