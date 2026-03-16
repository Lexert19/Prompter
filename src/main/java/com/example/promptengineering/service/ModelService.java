package com.example.promptengineering.service;

import com.example.promptengineering.dto.ModelDto;
import com.example.promptengineering.entity.Model;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.repository.ModelRepository;
import com.example.promptengineering.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ModelService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ModelRepository modelRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.max.models.per.user}")
    private int maxModelsPerUser;

    public List<Model> getUserModels(User user){
        List<Model> models = modelRepository.findByUser(user);
        if (models.size() >= maxModelsPerUser) {
            throw new IllegalArgumentException("User cannot have more than " + maxModelsPerUser + " models.");
        }
        return models;
    }

    public List<Model> getGlobalModels(){
        return this.modelRepository.findByGlobal(true);
    }

    public User addUserModel(ModelDto modelDto, User user){
        long currentCount = modelRepository.countByUser(user);
        if (currentCount >= maxModelsPerUser) {
            throw new IllegalArgumentException("User cannot have more than " + maxModelsPerUser + " models.");
        }

        Model model = new Model(modelDto.getName(), modelDto.getText(), modelDto.getProvider(), modelDto.getUrl(), modelDto.getType(), user);
        model.setGlobal(false);
        modelRepository.save(model);
        return userRepository.save(user);
    }

    public void deleteUserModel(Long id, User user) {
        Optional<Model> model = this.getModel(id);
        if (model.isPresent() && model.get().getUser().equals(user)) {
            modelRepository.delete(model.get());
        } else {
            throw new SecurityException("This is not your model.");
        }
    }

    public void editUserModel(Model model, ModelDto modelDto){
        model.setName(modelDto.getName());
        model.setText(modelDto.getText());
        model.setProvider(modelDto.getProvider());
        model.setUrl(modelDto.getUrl());
        model.setType(modelDto.getType());
        modelRepository.save(model);
    }
    public Optional<Model> getModel(Long id) {
        return modelRepository.findById(id);
    }

    @Transactional
    public void loadDefaultModelsFromJson() {
        try {
            modelRepository.deleteByGlobalTrue();
            InputStream is = getClass().getResourceAsStream("/default-models.json");
            List<ModelDto> defaultModels = objectMapper.readValue(is, new TypeReference<>() {
            });

            for (ModelDto dto : defaultModels) {
                boolean exists = modelRepository.existsByProviderAndName(dto.getProvider(), dto.getName());
                if (!exists) {
                    Model model = new Model();
                    model.setName(dto.getName());
                    model.setText(dto.getText());
                    model.setProvider(dto.getProvider());
                    model.setUrl(dto.getUrl());
                    model.setType(dto.getType());
                    model.setPointsPerInput(dto.getPointsPerInput());
                    model.setPointsPerOutput(dto.getPointsPerOutput());
                    model.setGlobal(true);
                    model.setUser(null);
                    modelRepository.save(model);
                    log.info("Added model: {} ({})", dto.getName(), dto.getProvider());
                } else {
                    log.debug("Model already exists: {} ({}) – skipping", dto.getName(), dto.getProvider());
                }
            }
        } catch (IOException e) {
            log.error("Failed to load default models", e);
        }
    }

}
