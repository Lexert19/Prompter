<#import "/spring.ftl" as spring />
<#include "/parts/links.ftl" />
<#include "/parts/navbar.ftl" />
<link rel="stylesheet" href="/static/css/admin.css">
<div class="mesh-background">
    <div class="cloud cloud-1"></div>
    <div class="cloud cloud-2"></div>
    <div class="cloud cloud-3"></div>
</div>
<div class="container" style="min-height: 100vh; padding-top: 80px;">
    <div class="content-wrapper d-flex flex-column align-items-center px-2"
         style="">
        <div class="panel">
            <h2 style="text-align: center; margin-bottom: 1.5rem;">Panel administratora – użytkownicy</h2>
            <table class="admin-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Email</th>
                        <th>Role</th>
                        <th>Akcje</th>
                    </tr>
                </thead>
                <tbody>
                    <#list users as user>
                    <tr>
                        <td>${user.id}</td>
                        <td>${user.email}</td>
                        <td>
                            <#list user.roles as role>${role}<#sep>, </#list>
                        </td>
                        <td class="admin-actions">
                            <form action="/admin/users/${user.id}/role" method="post" class="d-flex">
                                <select name="role" class="me-2">
                                    <#list availableRoles as r>
                                    <option value="${r}" <#if user.roles?seq_contains(r)>
                                        selected</#if>>${r}
                                    </option>
                                    </#list>
                                </select>
                                <button type="submit" class="btn-small">Zmień</button>
                            </form>
                            <form action="/admin/users/${user.id}/delete"
                                  method="post"
                                  style="display: inline-block"
                                  onsubmit="return confirm('Czy na pewno chcesz usunąć tego użytkownika?');">
                                <button type="submit" class="btn-small btn-danger">Usuń</button>
                            </form>
                        </td>
                    </tr>
                    </#list>
                    <#if users?size == 0>
                    <tr>
                        <td colspan="4" style="text-align: center; padding: 20px;">Brak użytkowników.</td>
                    </tr>
                    </#if>
                </tbody>
            </table>
            <div style="text-align: center; margin-top: 20px;">
                <a href="/"
                   class="open_chat"
                   style="padding: 10px 20px;
                          font-size: 1rem">
                    <i class="fas fa-arrow-left"></i> Powrót na stronę główną
                </a>
            </div>
        </div>
    </div>
</div>
<#include "/parts/footer.ftl" />
