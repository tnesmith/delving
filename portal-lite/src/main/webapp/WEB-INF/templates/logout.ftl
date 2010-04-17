<#import "spring.ftl" as spring />
<#assign thisPage = "logout.html"/>
<#include "inc_header.ftl">

<body>

<div id="sidebar" class="grid_3">

    <div id="identity">
            <h1>ICN</h1>
            <a href="index.html" title="ICN"><img src="images/logo-small.png" alt="ICN Home"/></a>
    </div>

</div>

<div id="main" class="grid_9">

    <div id="top-bar">
        <@userbar/>
    </div>

    <div class="clear"></div>


    <h3>You have successfully logged out</h3>

</div>


<#include "inc_footer.ftl"/>

</body>
</html>
