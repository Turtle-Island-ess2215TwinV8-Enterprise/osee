	function post(url, stateChangeHandler) {
		httpRequest("POST", url, stateChangeHandler);
	}
	
	function get(url, stateChangeHandler) {
		httpRequest("GET", url, stateChangeHandler);
	}
	
	function httpRequest(httpVerb, url, stateChangeHandler) {
		var httpRequest = new XMLHttpRequest()
		httpRequest.prevDataLength = 0;
		httpRequest.onreadystatechange = stateChangeHandler;
		httpRequest.open(httpVerb, url);
		httpRequest.send();
	}

	function removeChildren(node) {	
		while (node.hasChildNodes()) {
    		node.removeChild(node.lastChild);
		}
	}