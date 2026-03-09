<#import "/spring.ftl" as spring />

<nav class="navbar navbar-expand-lg position-fixed w-100 top-0" style="z-index: 1000;">
  <div class="container">
    <a class="navbar-brand text-white fs-4 fw-bold" href="/">Prompter</a>
    <button class="navbar-toggler text-white" type="button" data-bs-toggle="collapse" data-bs-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
      <span class="navbar-toggler-icon"></span>
    </button>
    <div class="collapse navbar-collapse py-3 py-lg-0" id="navbarSupportedContent">
        <div class="language-dropdown me-3 ms-auto">
            <button class="dropdown-toggle" id="langDropdownBtn" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                <span id="currentLangFlag"></span>
                <span id="currentLangText">PL</span>
            </button>
            <div class="dropdown-menu" id="langDropdownMenu" aria-labelledby="langDropdownBtn">
                <button class="dropdown-item" data-lang="pl">PL</button>
                <button class="dropdown-item" data-lang="en">EN</button>
            </div>
        </div>
        <a class="text-white open_chat py-2 px-3 text-decoration-none rounded-pill" href="/auth/login">
            <@spring.message "app.login" />

</a>
    </div>
  </div>
</nav>