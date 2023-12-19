# DLT_MarketSoln_Corda


<h3> Set up </h3>

<ul>
<li>Clone this git repo</li>
<li>Move into the project folder</li>
<li>Compile the code to create the jar files for all the nodes by <code>./gradlew clean deployNodes</code></li>
<li>The jar file for each node is individually available in it's respective folder in <code>./build/nodes/</code></li>
<li>To run all the nodes at the same time, in Linux/Mac type <code>./build/nodes/runnodes</code></li>
</ul>

<h3>Initial Steps</h3>
<p>After each node is up and running follow this initial steps</p>
<ul>
<li>In the <strong>Delivery</strong> node type <code>flow start CreateNewAccount accountName: Delivery, shareWith: [Bank,Shop,Buyer]</code></li>
<li>In the <strong>Bank</strong> node type <code>flow start CreateNewAccount accountName: Bank, shareWith: [Buyer,Shop,Delivery]</code></li>
</ul>

<h3>Starting the server</h3>
<p>The framework have integrated <strong>Spring Boot Framework</strong>. To start the server type</p>
<ul>
<li><code>./gradlew runBuyerServer</code></li>
<li><code>./gradlew runDeliveryServer</code></li>
<li><code>./gradlew runBankServer</code></li>
<li><code>./gradlew runShopServer</code></li>
</ul>

<h3>Demo</h3>
<p>The demo apis are available at below url. The cordaaps are hosted in GCP</p>
<ul>
<li>User Server: <code>http://34.71.53.216:10050/swagger-ui.html</code></li>
<li>Delivery Server: <code>http://34.71.53.216:10060/swagger-ui.html</code></li>
<li>Bank Server: <code>http://34.71.53.216:10070/swagger-ui.html</code></li>
<li>Shop Server: <code>http://34.71.53.216:10080/swagger-ui.html</code></li>
</ul>
<p>If the url isn't opening then it means the VMs have been discontinued.</p>