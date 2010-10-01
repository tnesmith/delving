<#compress>
    <#assign thisPage = "static-image.img"/>
    <#assign pageId = "static"/>
    <#include "inc_header.ftl"/>
    <div class="main">

        <div id="image" class="grid_12">

            <div class="static_image">
                <#if imageExists>
                    <img src="${imagePath}" alt="${imagePath}"/>
                <#else>
                    <p>This image does not exist</p>
                </#if>
                <#if edit??>
                    <#if edit>
                        <div id="pageForm">
                            <form method="POST" enctype="multipart/form-data">
                                <table>
                                    <tr>
                                        <td>Upload a new image for this URL</td>
                                        <td><input type="file" name="file" size="80"/></td>
                                    </tr>
                                    <tr>
                                        <td></td>
                                        <td><input type="submit" name="submit"></td>
                                    </tr>
                                </table>
                            </form>
                        </div>
                    <#else>
                        <p><a href="${imagePath}?edit=true">Change this image</a></p>
                    </#if>
                </#if>

            </div>

        </div>
    </div>
    <#include "inc_footer.ftl"/>
</#compress>