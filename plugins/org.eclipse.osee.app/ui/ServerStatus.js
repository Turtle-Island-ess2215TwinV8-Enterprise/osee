function statusHandler() {
	if (this.readyState == 3 || this.readyState == 4) {
		while (this.prevDataLength != this.responseText.length) {
			var index = this.responseText.indexOf('\n', this.prevDataLength);
			var nextLine = this.responseText.substring(this.prevDataLength, index);
			this.prevDataLength = index + 1;
 			singleStatus(nextLine);
 		}
	}
}

function singleStatus(jsonText) {
	var jsonObj = JSON.parse(jsonText);

	var statusNode = document.getElementById("serverStatus");
	if (jsonObj.state == "done") {
	   statusNode.firstChild.nodeValue = "ALIVE";
   }
}

get("dashboard/serverStatus", statusHandler);

