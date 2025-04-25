<div id="editModelMenu" class="model-menu" style="display: none">
    <div class="mb-1">
        <h2>Edytuj model</h2>
        <label class="block" for="modelName">Nazwa modelu</label>
        <input type="text" class="mb-05" id="editModelName" name="modelName">
        <label class="block" for="modelText">Opis modelu</label>
        <input type="text" class="mb-05" id="editModelText" name="modelText">
        <label class="block" for="modelProvider">Dostawca modelu</label>
        <input type="text" class="mb-05" id="editModelProvider" name="modelProvider">
        <label class="block" for="modelUrl">Adres URL modelu</label>
        <input type="text" class="mb-05" id="editModelUrl" name="modelUrl">
        <label class="block" for="modelType">Typ modelu</label>
        <input type="text" class="mb-05" id="editModelType" name="modelType">
        <button onclick="window.addEditModelMenu.editModel()">Edytuj model</button>
    </div>
</div>