<#import "/spring.ftl" as spring />
<#include "/parts/head.ftl" />
<div class="mesh-background">
    <div class="cloud cloud-1"></div>
    <div class="cloud cloud-2"></div>
    <div class="cloud cloud-3"></div>
</div>
<div class="slides-container">
    <section class="slide pt-5 pt-md-0" id="slide1">
        <div class="center content-wrapper">
            <div class="block content-box p-0 pt-5 w-100 hidden">
                <div class="chat-header d-flex mb-3 flex-column align-items-center"
                     style="text-align: center">
                    <img src="/favicon"
                         class="d-block"
                         width="70px"
                         height="70px"
                         style="vertical-align: middle;
                                margin-left: 10px;
                                filter: drop-shadow(2px 2px 3px rgba(0,0,0,0.3))">
                    <h1 style="margin: 0;font-weight: bold;">
                        <span class="text-shadow-md" style="color: var(--accent);">Prompter</span>
                    </h1>
                    <p class="text-white mt-3 text-shadow-lg">
                        <@spring.message "app.description" />
                    </p>
                    <div class="center mt-4 mb-1">
                        <a href="/chat"
                           class="open_chat text-decoration-none rounded-pill"
                           style="padding: 15px 30px;">
                            <i class="fas fa-comment-dots"></i>
                            <@spring.message "start_chat" />
                        </a>
                    </div>
                    <div class="container">
                    <div class="row g-4 my-md-4 my-2">
                        <div class="col-md-4">
                            <div class="card glass-card h-100 text-center">
                                <div class="card-body">
                                    <i class="fas fa-robot fa-3x mb-3" style="color: var(--accent);"></i>
                                    <h5 class="card-title text-white">
                                        <@spring.message "home.feature.apiTesting.title" />
                                    </h5>
                                    <p class="card-text text-white-50">
                                        <@spring.message "home.feature.apiTesting.desc" />
                                    </p>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="card glass-card h-100 text-center">
                                <div class="card-body">
                                    <i class="fas fa-leaf fa-3x mb-3" style="color: var(--accent);"></i>
                                    <h5 class="card-title text-white">
                                        <@spring.message "home.feature.springJava.title" />
                                    </h5>
                                    <p class="card-text text-white-50">
                                        <@spring.message "home.feature.springJava.desc" />
                                    </p>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="card glass-card h-100 text-center">
                                <div class="card-body">
                                    <i class="fab fa-js fa-3x mb-3" style="color: var(--accent);"></i>
                                    <h5 class="card-title text-white">
                                        <@spring.message "home.feature.vanillaJs.title" />
                                    </h5>
                                    <p class="card-text text-white-50">
                                        <@spring.message "home.feature.vanillaJs.desc" />
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>
                    </div>
                </div>

            </div>
        </div>
    </section>
    <section class="slide pt-5 pt-md-0" id="slide2">
        <div class="center content-wrapper">
            <div class="block content-box hidden">
                <div class="container">
                <div class="row g-4 align-items-center">
                    <div class="col-md-6 px-0 text-center text-md-start">
                        <img src="/static/images/screenshot_prompter.png"
                             alt="Screenshot aplikacji Prompter"
                             class="img-fluid rounded shadow-lg"
                             style="max-height: 60vh; width: auto;">
                    </div>
                    <div class="col-md-6 ps-md-5">
                        <h2 class="text-center mb-4" style="color: var(--accent);">How It Works</h2>
                        <div class="d-flex flex-column gap-2">
                            <div class="d-flex align-items-start">
                                <div>
                                    <h5>
                                        <i class="fas fa-key me-3 flex-shrink-0" style="color: var(--accent);"></i>
                                        <@spring.message "home.feature.byok.title" />
                                    </h5>
                                    <p class="text-white-50 mb-0"><@spring.message "home.feature.byok.desc" /></p>
                                </div>
                            </div>
                            <div class="d-flex align-items-start">
                                <div>
                                    <h5>
                                        <i class="fas fa-robot me-3 flex-shrink-0" style="color: var(--accent);"></i>
                                        <@spring.message "home.feature.models.title" /></h5>
                                    <p class="text-white-50 mb-0"><@spring.message "home.feature.models.desc" /></p>
                                </div>
                            </div>
                            <div class="d-flex align-items-start">
                                <div>
                                    <h5>
                                        <i class="fas fa-history me-3 flex-shrink-0" style="color: var(--accent);"></i>
                                        <@spring.message "home.feature.history.title" /></h5>
                                    <p class="text-white-50 mb-0"><@spring.message "home.feature.history.desc" /></p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                </div>
            </div>
        </div>
    </section>
</div>
<script src="/static/js/home.js"></script>
<#include "/parts/footer.ftl" />
