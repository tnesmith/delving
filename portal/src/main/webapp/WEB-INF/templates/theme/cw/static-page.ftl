<#compress>
    <#if pagePathList??>
        <#assign thisPage = "static-page.html"/>
        <#assign pageId = "static"/>
        <#include "inc_header.ftl"/>
        <div class="main">
        <ul>
            <#list pagePathList as pagePath>
                <li><a href="${pagePath}">${pagePath}</a></li>
            </#list>
        </ul>
        </div>
        <#include "inc_footer.ftl"/>
    <#elseif onlyContent??>
        ${content}
    <#else>
        <#assign thisPage = "static-page.html"/>
        <#assign pageId = "static"/>
        <#include "inc_header.ftl"/>
        <div class="main">

            <div id="search" class="grid_12">

                <div class="static_page">

                    <#if content??>
                        <div id="content" class="content-preview">
                        ${content}
                        </div>
                    </#if>
                    <#if edit??>
                        <#if edit>
                            <div id="pageForm">

                                <form action="${pagePath}" method="POST">

                                                <textarea name="content" id="editor" style="width:100%;height:350px;"${content}</textarea>

                                                <input type="submit" name="submit">
                                                    <a href="javascript:toggleEditor('editor');">Show/Hide HTML editor</a>
                                </form>
                            </div>
                            <#else>
                                <p><a href="${pagePath}?edit=true" class="button">Edit this page.</a></p>
                        </#if>
                    </#if>

                </div>

            </div>
        </div>
        <#include "inc_footer.ftl"/>
         <script type="text/javascript" src="/${portalName}/${portalTheme}/js/tiny_mce/tiny_mce.js"></script>
    <script type="text/javascript">

    tinyMCE.init({
        mode : "textareas",
        theme : "advanced",
        theme_advanced_toolbar_location : "top",
        theme_advanced_toolbar_align : "left",
        theme_advanced_buttons1 : "bold,italic,underline,strikethrough,|,justifyleft,justifycenter,justifyright,justifyfull,|,formatselect|,bullist,numlist,|,undo,redo,|,link,unlink,anchor,|,image,|,forecolor,backcolor",
        theme_advanced_buttons2 : "",
        theme_advanced_statusbar_location : "bottom",
        content_css : "/${portalName}/${portalTheme}/css/reset-text-grid.css,/${portalName}/${portalTheme}/css/type.css,/${portalName}/${portalTheme}/css/color.css,/${portalName}/${portalTheme}/css/screen.css"
    });

    function toggleEditor(id) {

        if (!tinyMCE.get(id)){
            tinyMCE.execCommand('mceAddControl', false, id);
        }else{
            tinyMCE.execCommand('mceRemoveControl', false, id);
        }
    }
    </script>
    </#if>
</#compress>