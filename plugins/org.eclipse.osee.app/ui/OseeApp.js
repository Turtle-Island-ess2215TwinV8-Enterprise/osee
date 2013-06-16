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
			
		function postWithStatus(url) {
			httpRequest("POST", url, statusHandler);
		}
	
		function singleStatus(jsonText) {
			var jsonObj = JSON.parse(jsonText);
	
			if (jsonObj.state == "running") {
				var textNode2 = document.createTextNode(jsonObj.message);
				var logNode = document.getElementById("log");
				logNode.appendChild(textNode2);
		
				var br = document.createElement("br");
				logNode.appendChild(br);
			} else if (jsonObj.state == "done") {
				appCancel();
			}
			
			var statusNode = document.getElementById("status");
			statusNode.firstChild.nodeValue = jsonObj.status;
		}
	
		function startStopListener() {
			var button = document.getElementById("startToggleId");
			if (button.value == "Start") {
				appStart();
			} else {
				appCancel();
			}
		}
		
		function appCancel() {
		   var button = document.getElementById("startToggleId");
	   	button.value = "Start";
		}
		
		function appStart() {
			removeChildren(document.getElementById("log"));
			var button = document.getElementById("startToggleId");
		   button.value = "Cancel";
		   
		   oseeAppStart(getOseeAppParams());

		}
		
		function getOseeAppParams() {
			var params = new Object();
			var form = document.getElementById("oseeAppForm");
			var inputElements = form.getElementsByTagName("input");
			
			for(var i = 0; i < inputElements.length; i++) {
			var inputElement = inputElements[i];
				var id = inputElement.getAttribute("id");
				if (id == null) {
					console.log(inputElement);
					 console.log("has no id");
				} else {
					params[id] = inputElement.value;
				}
			}
			return params;
		}

		var button = document.getElementById("startToggleId");
		button.addEventListener("click", startStopListener);