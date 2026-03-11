<#import "/spring.ftl" as spring />
<#include "/parts/head.ftl" />
<div class="mesh-background">
    <div class="cloud cloud-1"></div>
    <div class="cloud cloud-2"></div>
    <div class="cloud cloud-3"></div>
</div>
<div class="slides-container">
    <section class="slide" id="slide1">
        <div class="center content-wrapper" style="height: 100vh;">
            <div class="block content-box hidden"
                 style="max-width: 600px;
                        padding: 20px">
                <div class="chat-header d-flex flex-column align-items-center"
                     style="border-radius: 10px;
                            margin-bottom: 24px;
                            text-align: center">
                    <img src="/favicon"
                         class="d-block"
                         width="90px"
                         height="90px"
                         style="vertical-align: middle;
                                margin-left: 10px;
                                filter: drop-shadow(2px 2px 3px rgba(0,0,0,0.3))">
                    <h1 style="margin: 0; font-size: 4rem; font-weight: bold;">
                        <span class="text-shadow-md" style="color: var(--accent);">Prompter</span>
                    </h1>
                    <p class="text-white fs-5 text-shadow-lg"
                       style="margin-top: 10px;
                              font-size: 1.2rem">
                        <@spring.message "app.description" />
                    </p>
                </div>
                <div class=" center mb-1">
                    <a href="/chat"
                       class="open_chat text-decoration-none rounded-pill"
                       style="padding: 15px 30px;
                              font-size: 1.1rem">
                        <i class="fas fa-comment-dots"></i>
                        <@spring.message "start_chat" />
                    </a>
                </div>
            </div>
        </div>
    </section>
    <section class="slide" id="slide2">
        <div class="center content-wrapper" style="height: 100vh;">
            <div class="block content-box hidden"
                 style="max-width: 800px;
                        padding: 30px">
                <h2 class="text-center mb-4" style="color: var(--accent);">How It Works</h2>
                <div class="row g-4">
                    <div class="col-md-6">
                        <div class="d-flex align-items-start">
                            <i class="fas fa-key fa-2x me-3" style="color: var(--accent);"></i>
                            <div>
                                <h5>
                                    <@spring.message "home.feature.byok.title" />
                                </h5>
                                <p class="text-white-50">
                                    <@spring.message "home.feature.byok.desc" />
                                </p>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="d-flex align-items-start">
                            <i class="fas fa-robot fa-2x me-3" style="color: var(--accent);"></i>
                            <div>
                                <h5>
                                    <@spring.message "home.feature.models.title" />
                                </h5>
                                <p class="text-white-50">
                                    <@spring.message "home.feature.models.desc" />
                                </p>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="d-flex align-items-start">
                            <i class="fas fa-history fa-2x me-3" style="color: var(--accent);"></i>
                            <div>
                                <h5>
                                    <@spring.message "home.feature.history.title" />
                                </h5>
                                <p class="text-white-50">
                                    <@spring.message "home.feature.history.desc" />
                                </p>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="d-flex align-items-start">
                            <i class="fas fa-project-diagram fa-2x me-3"
                               style="color: var(--accent)"></i>
                            <div>
                                <h5>
                                    <@spring.message "home.feature.projects.title" />
                                </h5>
                                <p class="text-white-50">
                                    <@spring.message "home.feature.projects.desc" />
                                </p>
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
