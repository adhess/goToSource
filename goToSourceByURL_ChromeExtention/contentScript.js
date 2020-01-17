chrome.runtime.onMessage.addListener(function (msg, sender, sendResponse) {
    if (msg.target.toString() === 'selectors') {
        var arr = {};
        var j = 0;
        var relatedComponents = msg.relatedComponents;
        relatedComponents = relatedComponents.substring(1, relatedComponents.length - 1);
        var selectors = relatedComponents.split(',');
        for (var i = 0; i < selectors.length; i++) {
            var elements = document.getElementsByTagName(selectors[i].trim());
            for (var elt of elements) {
                // elt.style.backgroundColor='#ff000052';
                var obj = {};
                obj['border'] = elt.style.border;
                obj['filter'] = elt.style.filter;
                obj['display'] = elt.style.display;
                arr[selectors[i].trim() + '-' + j++] = obj;

                elt.style.border = '1px solid #ff000040';
                elt.style.filter = 'brightness(0.9)';
                elt.style.display = 'block';

                elt.onmouseover = function () {
                    this.style.filter = 'brightness(1)';
                };
                elt.onmouseout = function () {
                    this.style.filter = 'brightness(0.9)';
                };
                elt.onclick = function () {
                    var localName = this.localName;
                    chrome.runtime.sendMessage({selector: localName, target: 'goToComponentBySelector'}, function (response) {
                        console.log("response: ", response);
                    });
                    console.log(arr);
                    var j = 0;
                    for (var i = 0; i < selectors.length; i++) {
                        var elements = document.getElementsByTagName(selectors[i].trim());
                        for (var elt of elements) {
                            var s = elt.localName + '-' + j++;
                            console.log(s);
                            var obj = arr[s];
                            elt.style.border = obj['border'];
                            elt.style.filter = obj['filter'];
                            elt.style.display = obj['display'];

                            elt.onmouseover = undefined;
                            elt.onmouseout = undefined;
                            elt.onclick = undefined;
                        }
                    }

                };
            }
        }

        console.log();
        console.log("Received %o from %o, frame", msg, sender.tab, sender.frameId);
        sendResponse("Gotcha!");
    }

});