<table width="100%" height="192px" style="position: relative">
    <tr>
        <td width="192px">
            <% if ('images' in object && object.images.length > 0) { %>
            <div class="cover" style="background-image: url('<%=object.images[0].src%>'); width: 192px; height: 192px"></div>
            <% } else { %>
            <div class="cover" style="width: 192px; height: 192px; position: relative">
                <i class="fa fa-cube"></i>
            </div>
            <% } %>
        </td>
        <td valign="top">
            <small class="sp-type"><%=object.type%></small>
            <h1><a data-uri="<%=object.uri%>"><%=object.name%></a></h1>
            <p class="description"><%=object.description%></p>
            <div class="sp-toolbar" style="bottom: 0px; position: absolute">
                <button class="btn btn-primary">Action</button>
                <button class="btn">...</button>
            </div>
        </td>
    </tr>
</table>