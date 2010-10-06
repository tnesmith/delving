<#import "spring.ftl" as spring >
<#if user??>
    <#assign user = user/>
</#if>
<#assign useCache = "false">
<#if RequestParameters.useCache??>
    <#assign useCache = "${RequestParameters.useCache}"/>
</#if>
<#-- used for grabbing locale based includes and images -->
<#-- locale also used on homepage for language based announcements/disclaimers -->
<#assign locale = springMacroRequestContext.locale>
<#assign query = "">
<#assign cacheUrl = cacheUrl>
<#assign view = "table">
<#assign enableRefinedSearch="true"/>
<#if RequestParameters.view??>
    <#assign view = "${RequestParameters.view}">
</#if>
<#if RequestParameters.query??>
    <#assign query = "${RequestParameters.query}">
</#if>
<#if format??>
    <#assign format = format>
</#if>
<#assign interfaceLanguage = interfaceLanguage/>
<#macro localeBased type toSwitch>
    <#-- if locale is something like en_us, then shorten to en -->
    <#if locale?length &gt; 2>
        <#assign locale =  locale?substring(0, 2)>
    </#if>
    <#if type == "include">
        <#-- current available languages -->
        <#assign langs = ["ca","cs","da","de","el","en","es","et","fi","fr","ga","hu","is","it","lt","lv","mt","nl","no","pl","pt","sk","sl","sv"]>
    <#if toSwitch == "inc_usingeuropeana_$$.ftl">
    <#-- current available languages for this particular file -->
        <#assign langs = ["de","en","es","fr"]>
    </#if>
    <#if langs?seq_index_of(locale) != -1>
        <#include "${toSwitch?replace('$$', locale)}">
    <#else>
        <#include "${toSwitch?replace('$$','en')}">
    </#if>
    <#else>
        <img src="/${portalName}/${portalTheme}/${toSwitch?replace('$$',locale)!'think_culture_en_small.gif'}" alt=""/>
    </#if>
</#macro>

<#-- shorten a given string and append with ellipses -->
<#macro stringLimiter theStr size>
    <#assign newStr = theStr>
    <#if newStr?length &gt; size?number>
    <#assign newStr = theStr?substring(0,size?number) + "...">
    </#if>
    ${newStr}
</#macro>

<#macro SearchForm className>

    <#assign showAdv="none"/>
    <#assign showSim="block"/>
    <#if pageId??>
        <#if pageId=="adv">
            <#assign showAdv="block"/>
            <#assign showSim="none"/>
        </#if>
    </#if>

    <div id="search_simple" class="${className}" style="display:${showSim};">
        <#if result?? >
            <#if result.badRequest?? >
                <span style="font-style: italic;">Wrong query. ${result.errorMessage}</span>
            </#if>
        </#if>
        <form method="get" action="/${portalName}/brief-doc.html" accept-charset="UTF-8" onsubmit="return checkFormSimpleSearch('query');">
            <input type="hidden" name="start" value="1" />
            <input type="hidden" name="view" value="${view}" />
            <select name="zoeken_in" class="form_11">
                    <option selected="selected" value="text">Alles</option>
                    <option value="title">Titel</option>
                    <option value="creator">Vervaardiger</option>
                    <option value="dc_subject">Onderwerp</option>
                    <option value="COLLECTION">Collectie</option>
                  </select>
            <input class="txt-input" name="query" id="query" type="text" title="${portalDisplayName} Search" maxlength="75" />
            <input id="submit_search" type="submit" value="<@spring.message 'Search_t' />" />
            <a href="/${portalName}/advancedsearch.html" id="href-advanced" title="<@spring.message 'AdvancedSearch_t' />"><@spring.message 'AdvancedSearch_t' /></a>
        </form>
        <#--<#if query?? && query?length &gt; 0 && enableRefinedSearch??>-->
            <#--<a class="advanced-search" href="" onclick="toggleObject('search_simple');toggleObject('search_refine');return false;" title="Refine Search" rel="nofollow">Refine Search</a>-->
        <#--</#if>-->
    </div>
</#macro>

<#macro userbar>
    <ul class="user_nav">
        <#if !user??>
            <li id="mustlogin" class="msg"><a href="/${portalName}/login.html?pId=${pageId}"><@spring.message 'LogIn_t'/></a></li>
            <li><a href="/${portalName}/login.html?pId=${pageId}"><@spring.message 'Register_t'/></a></li>
        </#if>
        <#if user??>

            <li>
                <@spring.message 'LoggedInAs_t' />: <strong>${user.userName?html}</strong> | <a
                    href="/${portalName}/logout.html"><@spring.message 'LogOut_t' /></a>
            </li>

            <#if user.savedItems??>
                <li>
                    <a href="/${portalName}/mine.html" onclick="$.cookie('ui-tabs-3', '1', { expires: 1 });" id="href-saved-items">
                        <@spring.message 'SavedItems_t' />
                    </a>
                    (<span id="savedItemsCount">${user.savedItems?size}</span>)
                </li>
            </#if>
            <#if user.savedSearches??>
                <li>
                    <a href="/${portalName}/mine.html" onclick="$.cookie('ui-tabs-3', '2', { expires: 1 });" id="href-saved-searches">
                        <@spring.message 'SavedSearches_t' />
                    </a>
                    (<span id="savedSearchesCount">${user.savedSearches?size}</span>)
                </li>
            </#if>
            <#if user.socialTags??>
                <li>
                    <a href="/${portalName}/mine.html" onclick="$.cookie('ui-tabs-3', '3', { expires: 1 });"  id="href-saved-tags">
                        <@spring.message 'SavedTags_t' />
                    </a>
                    (<span id="savedTagsCount">${user.socialTags?size}</span>)
                </li>
            </#if>
        </#if>
        <li><a href="/${portalName}/hulp-nodig.dml">Hulp nodig?</a></li>
        <li><a href="/${portalName}/contact.dml">Contact</a></li>
    </ul>
</#macro>

<#macro admin>
    <#if user?? && (user.role == ('ROLE_ADMINISTRATOR') || user.role == ('ROLE_GOD'))>
    <div id="admin-block">
        <h4>Administratie</h4>

        <table class="user-options">
            <tbody>
                <tr>
                    <td><a href="/${portalName}/_.dml"><span class="ui-icon ui-icon-document"></span>Paginas beheren</a></td>
                </tr>
                <tr>
                    <td><a href="/${portalName}/_.img"><span class="ui-icon ui-icon-image"></span>Afbeeldingen beheren</a></td>
                </tr>
                <tr>
                    <td><a href="/${portalName}/administration.html"><span class="ui-icon ui-icon-person"></span>Gebruikers beheren</a></td>
                </tr>
            </tbody>
        </table>

    </div>
    </#if>
</#macro>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <link rel="shortcut icon" href="/${portalName}/favicon.ico"/>
    <link rel="stylesheet" type="text/css" href="/${portalName}/${portalTheme}/css/reset-text-grid.css"/>
    <#if portalColor??>
        <link rel="stylesheet" type="text/css" href="/${portalName}/${portalTheme}/css/${portalColor}/jquery-ui-1.8.5.custom.css" />
    <#else>
        <link rel="stylesheet" type="text/css" href="/${portalName}/${portalTheme}/css/jquery-ui-1.8.5.custom.css"/>
    </#if>
    <link rel="stylesheet" type="text/css" href="/${portalName}/${portalTheme}/css/colors.css"/>
    <link rel="stylesheet" type="text/css" href="/${portalName}/${portalTheme}/css/type.css" />
    <link rel="stylesheet" type="text/css" href="/${portalName}/${portalTheme}/css/screen.css"/>
    <link rel="stylesheet" type="text/css" href="/${portalName}/${portalTheme}/css/print.css" media="print"/>
    <script type="text/javascript">
        var msgRequired = "<@spring.message 'RequiredField_t'/>";
        var portalName = "/${portalName}";
        var baseThemePath = "/${portalName}/${portalTheme}";
    </script>
    <script type="text/javascript" src="/${portalName}/${portalTheme}/js/jquery-1.4.2.min.js"></script>
    <script type="text/javascript" src="/${portalName}/${portalTheme}/js/jquery-ui-1.8.5.custom.min.js"></script>
    <script type="text/javascript" src="/${portalName}/${portalTheme}/js/jquery.cookie.js"></script>
    <script type="text/javascript" src="/${portalName}/${portalTheme}/js/jquery.toggleElements.js"></script>
    <script type="text/javascript" src="/${portalName}/${portalTheme}/js/jquery.validate.js"></script>
    <script type="text/javascript" src="/${portalName}/${portalTheme}/js/js_utilities.js"></script>
    <script type="text/javascript" src="/${portalName}/${portalTheme}/js/results.js"></script>
    <#switch thisPage>
    <#case "index.html">
    <#assign pageId = "in"/>
    <#assign bodyId = "home"/>
    <title>Instituut Collectie Nederland</title>
    <script type="text/javascript" src="/${portalName}/${portalTheme}/js/index.js"></script>
    <#break>
    <#case "advancedsearch.html">
    <#assign pageId = "adv"/>
    <#assign bodyId = "advancedsearch"/>
    <title>${portalDisplayName} - Advanced Search</title>
    <#break>
    <#case "brief-doc.html">
    <#assign pageId = "brd"/>
    <script type="text/javascript">
        var msgSearchSaveSuccess = "<@spring.message 'SearchSaved_t'/>";
        var msgSearchSaveFail = "<@spring.message 'SearchSavedFailed_t'/>";

        $(document).ready(function() {
            $('div.toggler-c').toggleElements(
            { fxAnimation:'slide', fxSpeed:'fast', className:'toggler' });
        });

    </script>
    <title>Instituut Collectie Nederland - Search results</title>
    <#break>
    <#case "full-doc.html">
    <#assign pageId = "fd"/>
    <script type="text/javascript" src="/${portalName}/${portalTheme}/js/fancybox/jquery.fancybox-1.3.1.pack.js"></script>
    <link rel="stylesheet" type="text/css" href="/${portalName}/${portalTheme}/css/fancybox/jquery.fancybox-1.3.1.css"/>
    <!--[if IE]>
    <style type="text/css">
        /* IE */
        #fancybox-loading.fancybox-ie div	{ background: transparent; filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_loading.png', sizingMethod='scale'); }
        .fancybox-ie #fancybox-close		{ background: transparent; filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_close.png', sizingMethod='scale'); }
        .fancybox-ie #fancybox-title-over	{ background: transparent; filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_title_over.png', sizingMethod='scale'); zoom: 1; }
        .fancybox-ie #fancybox-title-left	{ background: transparent; filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_title_left.png', sizingMethod='scale'); }
        .fancybox-ie #fancybox-title-main	{ background: transparent; filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_title_main.png', sizingMethod='scale'); }
        .fancybox-ie #fancybox-title-right	{ background: transparent; filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_title_right.png', sizingMethod='scale'); }
        .fancybox-ie #fancybox-left-ico		{ background: transparent; filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_nav_left.png', sizingMethod='scale'); }
        .fancybox-ie #fancybox-right-ico	{ background: transparent; filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_nav_right.png', sizingMethod='scale'); }
        .fancybox-ie .fancy-bg { background: transparent !important; }
        .fancybox-ie #fancy-bg-n	{ filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_shadow_n.png', sizingMethod='scale'); }
        .fancybox-ie #fancy-bg-ne	{ filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_shadow_ne.png', sizingMethod='scale'); }
        .fancybox-ie #fancy-bg-e	{ filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_shadow_e.png', sizingMethod='scale'); }
        .fancybox-ie #fancy-bg-se	{ filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_shadow_se.png', sizingMethod='scale'); }
        .fancybox-ie #fancy-bg-s	{ filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_shadow_s.png', sizingMethod='scale'); }
        .fancybox-ie #fancy-bg-sw	{ filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_shadow_sw.png', sizingMethod='scale'); }
        .fancybox-ie #fancy-bg-w	{ filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_shadow_w.png', sizingMethod='scale'); }
        .fancybox-ie #fancy-bg-nw	{ filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_shadow_nw.png', sizingMethod='scale'); }

    </style>
    <![endif]-->

    <#if user??>
    <script type="text/javascript">
        var msgItemSaveSuccess = "<@spring.message 'ItemSaved_t' />";
        var msgItemSaveFail = "<@spring.message 'ItemSaveFailed_t' />";
        var msgEmailSendSuccess = "<@spring.message 'EmailSent_t' />";
        var msgEmailSendFail = "<@spring.message 'EmailSendFailed_t' />";
        var msgEmailValid = "<@spring.message 'EnterValidEmail_t' />";
    </script>
    </#if>
    <title>Instituut Collectie Nederland - Search results</title>
    <#break>
    <#case "mine.html">
    <#assign pageId = "me"/>
    <link rel="stylesheet" type="text/css" href="/${portalName}/${portalTheme}/css/myeuropeana.css"/>
    <script type="text/javascript" src="/${portalName}/${portalTheme}/js/myEuropeana.js"></script>
    <script type="text/javascript">

        $(document).ready(function() {
            $("#savedItems").tabs('select', $.cookie('ui-tabs-3'));
            $("#savedItems").tabs({ cookie: { expires: 30 } });
        });
    </script>
    <title>Instituut Collectie Nederland - Mijn Gegevens</title>
    <#break>
    <#case "exception.html">
    <title>${portalDisplayName} - Exception</title>
    <#break>
    <#case "login.html">
    <#assign pageId = "li"/>
    <link rel="stylesheet" type="text/css" href="/${portalName}/${portalTheme}/css/login-register.css"/>
    <script type="text/javascript">
        $(document).ready(function() {
            $("#loginForm").validate({
                rules: {j_username: "required",j_password: "required"},
                messages: {j_username: "",j_password: ""}
            });
            $("#forgotemailForm").validate({
                rules: {email: "required"},
                messages: {email: ""}
            });
            $("#registrationForm").validate({
                rules: {email: "required",iagree: "required"},
                messages: {email: "",iagree: msgRequired }
                //msgRequired is generated in inc_header.ftl and
                // set as a javascript variable (its a spring message
                // so cannot be generated in this js file
            });
        });
    </script>
    <title>Instituut Collectie Nederland - Login</title>
    <#break>
    <#case "logout.html">
    <#assign pageId = "lo"/>
    <title>Instituut Collectie Nederland - Logout</title>
    <#break>
    <#case "register.html">
    <#assign pageId = "rg"/>
    <link rel="stylesheet" type="text/css" href="/${portalName}/${portalTheme}/css/login-register.css"/>
    <title>Instituut Collectie Nederland - Registration</title>
    <#break>
    <#case "forgotPassword.html">
    <link rel="stylesheet" type="text/css" href="/${portalName}/${portalTheme}/css/login-register.css"/>
    <#assign pageId = "fp"/>
    <title>Instituut Collectie Nederland - Forgot Password</title>
    <#break>
    <#case "register-success.html">
    <link rel="stylesheet" type="text/css" href="/${portalName}/${portalTheme}/css/login-register.css"/>
    <title>Instituut Collectie Nederland - Registration continued</title>
    <#break>
    </#switch>

</head>

<body>

<div class="skin">
<div class="top">&#160;</div>
<div class="container_12 page <#if portalColor??>${portalColor}</#if>">


    <@admin/>

        <div id="user-bar" class="grid_12">
        <@userbar/>
    </div>

    <div class="header">
        <div class="branding">
            <img src="/${portalName}/${portalTheme}/images/RO_OCW_ICN.png" id="logo-home" alt="${portalDisplayName}" width="500"/>
        </div>
        <div class="clear"></div>
        <div class="title_bar">
            <div class="grid_12">

                Instituut Collectie Nederland  <em>De Collectie</em>
            </div>
        </div>
        <div class="clear"></div>
        <div class="nav_bar">
            <div class="grid_12">

                <ul class="nav_main">
                    <li <#if pageId="in">class="current"</#if>><a href="/${portalName}/index.html">Home</a></li>
                    <li><a href="/${portalName}/digitale-collectie.dml">Over de Digitale Collectie Nederland</a></li>
                    <li><a href="/${portalName}/deelname.dml">Deelname</a></li>
                    <li><a href="/${portalName}/gebruik.dml">Gebruik</a></li>
                </ul>
            </div>
        </div>
        <div class="clear"></div>
        <div class="nav_bottom">

        </div>

    </div>