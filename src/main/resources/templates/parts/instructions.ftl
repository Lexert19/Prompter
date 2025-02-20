<div class="chat-settings" id="chatSettings">
    <div class="instruction-field ">
        <label class="checkbox-container">
            <input type="checkbox" id="memory" name="memory" value="memory" onchange="window.settings.change(event)">
            <label for="memory" class="custom-checkbox"></label>
            <span>Pamięć (pamięta poprzednie wiadomości)</span>
        </label>
    </div>
    <div  class="instruction-field ">
        <label class="checkbox-container">
            <input type="checkbox" id="cache" name="cache" value="cache" onchange="window.settings.change(event)">
            <label for="cache" class="custom-checkbox"></label>
            <span>Cache</span>
        </label>
    </div>
    <div  class="instruction-field ">
        <input type="number" name="maxTokens" value="8000" onchange="window.settings.change(event)">
    </div>
    <div  class="instruction-field ">
        <label for="temperature"><span>Temperatura</span></label>
        <input type="range" id="temperature" name="temperature" min="0" max="100" step="1" value="0"
            oninput="window.settings.change(event)">
        
    </div>
    <div  class="instruction-field ">
        <label for="system">System</label>
        <div class="d-flex mt-025">
            <textarea id="system" name="system" onchange="window.settings.change(event)"></textarea>
            <label class="checkbox-container">
                <input type="checkbox" id="systemSwitch" name="systemSwitch" onchange="window.settings.change(event)">
                <label for="systemSwitch" class="custom-checkbox"></label>
            </label>
        </div>
    </div>
    <div  class="instruction-field ">
        <label>Projekt</label>
        <div class="d-flex mt-025">
            <select id="project" name="project" onchange="window.settings.change(event)"></select>
             <label class="checkbox-container">
                <input type="checkbox" id="projectSwitch" name="projectSwitch" onchange="window.settings.change(event)">
                <label for="projectSwitch" class="custom-checkbox"></label>
            </label>
        </div>
    </div>
    <div class="instruction-field" >
        <select id="modelOptions" name="model" class="mb-1" onchange="window.settings.changeModel(event)">
        </select>
    </div>
</div>
