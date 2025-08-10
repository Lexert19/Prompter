<div id="addModelMenu" class="model-menu" style="display:none">
    <div class="mb-1">
        <h2>Dodaj model</h2>
        <label class="block" for="modelName">Nazwa modelu</label>
        <input type="text" class="mb-05" id="modelName" name="modelName">
        <label class="block" for="modelText">Opis modelu</label>
        <input type="text" class="mb-05" id="modelText" name="modelText">
        <label class="block" for="modelProvider">Dostawca modelu</label>
        <select class="mb-05" id="modelProvider" name="modelProvider">

        </select>
        <label class="block" for="modelUrl">Adres URL modelu</label>
        <input type="text" class="mb-05" id="modelUrl" name="modelUrl">
        <label class="block" for="modelType">Typ modelu</label>
        <select  class="mb-05" id="modelType" name="modelType">
            <option value="text" selected>Text</option>
            <option value="vision">Vision</option>
        </select>
        <button onclick="window.addEditModelMenu.addModel()">Dodaj model</button>
    </div>
</div>