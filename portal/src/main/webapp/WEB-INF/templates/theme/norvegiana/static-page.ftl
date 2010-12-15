<#if pagePathList??>
<#--
 # CALLING THE PAGE FROM JAVASCRIPT
 # Give me some json please!
-->
    <#if RequestParameters.javascript??>
    <#compress>
        var tinyMCELinkList = new Array(
            <#list pagePathList as pagePath>
            ["${pagePath}","${pagePath}"]<#if pagePath_has_next>,</#if>
            </#list>
        );
    </#compress>
    <#else>
        <#--
         # INITIAL VIEW OF PAGE ADMINISTRATOR
         # Show list of pages and form to make new page
        -->
        <#assign thisPage = "static-page.dml"/>
        <#assign pageId = "static"/>
        <#include "includeMarcos.ftl">

        <@addHeader "${portalDisplayName}", "",[],[]/>

        <section role="main" class="main">

            <div class="grid_8">
                <h1><@spring.message '_cms.administration.pages' /></h1>

                <table summary="List of existing pages" class="user-pages zebra" width="100%">
                    <thead>
                    <tr>
                        <th colspan="3"><@spring.message '_cms.existing.pages' /></th>
                    </tr>
                    </thead>
                    <#list pagePathList as pagePath>
                        <tr id="${pagePath_index}">
                            <td>
                                <a href="${pagePath}?edit=true">
                                    <span class="ui-icon ui-icon-document"></span>
                                    <span class="ui-icon ui-icon-document"></span>
                                ${pagePath}</a></td>
                            <td width="100"><a href="${pagePath}?edit=true" class="btn-strong">
                                <span class="ui-icon ui-icon-pencil"></span>
                            <@spring.message '_cms.edit' /></a>
                            </td>
                            <td width="100">
                                <a class="delete" id="${pagePath_index}" href="${pagePath}?delete=true">
                                    <span class="ui-icon ui-icon-trash"></span>
                                <@spring.message '_cms.delete' />
                                </a>

                            </td>
                        </tr>
                    </#list>
                </table>
            </div>

            <div class="grid_4">

                <h2><@spring.message '_cms.page.create' /></h2>
                <ol>
                    <li><@spring.message '_cms.page.create.step.1' /></li>
                    <li><@spring.message '_cms.page.create.step.2' /></li>
                </ol>
                <form method="get" action="" id="form-makePage" onsubmit="createPage(this.pagePath.value);return false;">
                    /${portalName}/&#160;<input type="text" value="" name="pagePath" id="pagePath"/>
                    <input type="submit" value="<@spring.message '_cms.create' />" id="makePage"/>
                </form>

            </div>

        </section>

        <script type="text/javascript">

            $("a.delete").click(function() {
                var target = $(this).attr("id");
                var targetURL = $(this).attr("href");
                var confirmation = confirm("Pagina: " + targetURL + " verwijderen ?");
                if (confirmation) {
                    $.ajax({
                        url: targetURL,
                        type: "POST",
                        data: "content=",
                        success: function(data) {
                            $("table.user-pages tr#" + target).css("display", "none");
                            showMessage("success", "The page: " + targetURL + " has been deleted.");
                        },
                        error: function(data) {
                            showMessage("fail", "The page: " + targetURL + " could not be deleted.");
                        }
                    });
                    return false;
                }
                else {
                    return false;
                }
            });

            function createPage(page) {
                var targetURL = $("#pagePath").attr("value");
                window.location.href = targetURL + "?edit=true";
            }

            //                styleUIButtons();
        </script>

        <@addFooter/>

    </#if>

<#--
 # CALLING THE PAGE WITH THE PARAMATER 'embedded'
 @ page.dml?embedded=true
 # Handy for ajax calls to just get the page content
 # and not the header and footer
-->
<#elseif embedded>
    <#assign locale = locale/>
    ${page.getContent(locale)}
<#else>
<#--
 # EDITING A PAGE
 # Here user can add/edit page content
-->
    <#assign locale = locale/>
    <#assign thisPage = "static-page.dml"/>
    <#assign pageId = page.path/>
    <#include "includeMarcos.ftl">
    <@addHeader "${portalDisplayName}", "",["edit_area/edit_area_full.js"],[]/>

<section role="main" class="grid_12">

    <#if edit??>
        <#if edit>
            <h1><@spring.message '_cms.administration.pages' /></h1>
            <h6 class="ui-widget ui-state-highlight">${page.path}</h6>

            <form action="${page.path}" method="POST" id="form-edit" accept-charset="utf-8">
                <div class="ui-widget ui-widget-header ui-corner-all ui-helper-clearfix" style="padding: .5em; background:#e2e2e2">
                    <div style="float:left;">
                        <button id="page-submit" type="submit" class="button"><@spring.message '_cms.save' /></button>
                        <button id="page-resest" type="reset" class="button">Clear changes</button>
                        <button id="page-cancel" href="${page.path}" class="button"><@spring.message '_cms.cancel' /></button>
                        <a href="/${portalName}/_.dml" class="fg-button"><@spring.message '_cms.page.list' /></a>
                    </div>
                    <div style="float: right;">
                        <button id="edit-source" title="Edit Source" href="#" class="ui-state-active">Edit source</button>
                        <button id="edit-wysiwyg" title="Edit WYSIWYG" href="#">Edit wysiwyg</button>
                    </div>
                </div>
                <textarea name="content" id="editor" style="width: 100%;height:400px;">${page.getContent(locale)}</textarea>
            </form>

            <div class="clear"></div>
            <#else>
                <div id="content" class="content-preview">${page.getContent(locale)}</div>
                <div>
                    <#if page.id??>
                        <p>
                            <a href="${page.path}?edit=true&version=${page.id}" class="button"><@spring.message '_cms.page.edit' /></a>
                        </p>
                        <#else>
                            <p>
                                <a href="${page.path}?edit=true" class="button"><@spring.message '_cms.page.edit' /></a>
                            </p>
                    </#if>

                    <p><a href="/${portalName}/_.dml" class="button"><@spring.message '_cms.page.list' /></a></p>

                    <#if versionList?? && page.id??>
                        <div>
                            <hr/>
                            <h3><@spring.message '_cms.version.management' /></h3>

                            <p>
                            <@spring.message '_cms.version.approve.explain' />
                            </p>
                            <ul>
                                <#list versionList as version>
                                    <#if version.id == page.id>
                                        <li><strong>${version.date?string("yyyy-MM-dd HH:mm:ss")}</strong> -
                                            <a href="${version.path}?version=${version.id}&edit=false&approve=true"><@spring.message '_cms.version.approve' />
                                        </li>
                                        <#else>
                                            <li>
                                                <a href="${version.path}?version=${version.id}&edit=false">${version.date?string("yyyy-MM-dd HH:mm:ss")}</a>
                                            </li>
                                    </#if>
                                </#list>
                            </ul>
                        </div>
                    </#if>
                </div>
        </#if>
    </#if>
</section>

    <script type="text/javascript" src="/${portalName}/${portalTheme}/js/tiny_mce/tiny_mce_src.js"></script>
    <script type="text/javascript" src="/${portalName}/${portalTheme}/js/static-page.js"></script>
    <@addFooter/>
    </div>
</#if>
