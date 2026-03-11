<div class="instruction-field ">
    <label>
        <@spring.message "settings.thinking.effort" />
    </label>
    <select id="thinkingEffort"
            onchange="window.thinkingEffortView.change(event)">
        <option selected value="lack">
            <@spring.message "settings.thinking.effort.lack" />
        </option>
        <option value="none">
            <@spring.message "settings.thinking.effort.none" />
        </option>
        <option value="low">
            <@spring.message "settings.thinking.effort.low" />
        </option>
        <option value="medium">
            <@spring.message "settings.thinking.effort.medium" />
        </option>
        <option value="high">
            <@spring.message "settings.thinking.effort.high" />
        </option>
    </select>
</div>
