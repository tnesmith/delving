<#assign thisPage = "login.html"/>
<#--<#assign register = register>-->

<#include "includeMarcos.ftl">

<@addHeader "Norvegiana", "",[],[]/>

<#if contentOnly != "true">
    <div class="grid_12" id="branding">
        <h1 class="gigantic">
            <img src="/${portalName}/${portalTheme}/images/norvegiana.jpg" alt="Norvegiana" align="absmiddle"/>${portalDisplayName}
        </h1>
    </div>
</#if>

<section role="main" class="grid_4 prefix_4">
<h2><@spring.message 'LogIn_t' /></h2>

<form name='f1' id="loginForm" action='j_spring_security_check' method='POST' accept-charset="UTF-8">
<#if contentOnly = "true"><input type="hidden" name="ajax" value="true"/></#if>    
<table>
    <tr>
        <td><label for="j_username"><@spring.message 'EmailAddress_t' /></label></td>
        <td><input type='text' id="j_username" name="j_username" value="" maxlength="50"></td>
    </tr>
    <tr>
        <td><label for="j_password"><@spring.message "Password_t" /></label></td>
        <td><input type='password' id="j_password" name='j_password' maxlength="50"/></td>
    </tr>
    <tr>
        <td>
            <a href="/${portalName}/forgot-password.html"><@spring.message 'ForgotPassword_t' /></a>
        </td>
        <td align="right"><input name="submit_login" type="submit" value="<@spring.message 'LogIn_t' />"/></td>
    </tr>
</table>
<div id="login-err-msg"></div>

<#if errorMessage>

<strong><@spring.message 'Error_t' />: </strong> Inlog gegevens zijn niet juist

</#if>
</section>
<#if contentOnly = "true">
<script type="text/javascript">

        $("form#loginForm").submit(function(){
            $.ajax({
                url: "j_spring_security_check",
                type: "POST",
                data: $("#loginForm").serialize(),

                success: function(result) {
                    if (result == "fail") {
                       $('div#login-err-msg').html('<@spring.message 'login.failed' />')
                    }
                    else {
                       document.location.href="index.html";
                    }
                }
            });

            return false;
        })

</script>
</#if>
<@addFooter/>
