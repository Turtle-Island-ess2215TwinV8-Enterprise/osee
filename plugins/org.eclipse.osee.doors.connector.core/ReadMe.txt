*********************************************
	Doors OSLC Connector
*********************************************


OVERVIEW
********
Doors OSLC Connector is an open-source Eclipse plugins developed
by Robert Bosch Engineering and Business Solutions Ltd India. 
These plugins provide connectivity to DOORS and helps in linking doors requirements to OSEE artifacts. The connection is provided using OSLC .


USAGE
*****
These plugins will be deployed in the OSEE Client.
A menu item is contributed by these plugins. On clicking the menu item login dialog opens. On giving the credentials,Doors perspective should open with a view connected to DWA.
The user can select the requirement and click ok to cretae links in OSEE.


CONFIGURATION 
*************
Doors OSLC connector  provides an  extension point org.eclipse.osee.Doors.connector.core.DWAConstants to provide the DWAConstants to authenticate DWA. The contributing plugin should provide the necesary information to make connection to DWA like, Request Token Url, Authorization Url, DWA host name and so on.

Mozilla should be installed in the PC and XulRunner needs to be registered, if not configure any other available browsers which support HTML5 for the SWTBrowser Widget.


EXTERNAL JARS
*************
1. commons-codec-1.5.jar : http://www.apache.org/licenses/LICENSE-2.0
   License: Apache License. Licensce information is provided inside the 

2. httpclient-4.1.2.jar : http://www.apache.org/licenses/LICENSE-2.0
   License: Apache License. Licensce information is provided inside the jar.

3. scribe-1.3.0.jar:
   License: The MIT License

	Copyright (c) 2010 Pablo Fernandez

	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included in
	all copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
	THE SOFTWARE.
