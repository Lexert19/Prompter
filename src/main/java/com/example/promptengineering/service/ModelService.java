package com.example.promptengineering.service;

import com.example.promptengineering.dto.ModelDto;
import com.example.promptengineering.entity.Model;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.repository.ModelRepository;
import com.example.promptengineering.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModelService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ModelRepository modelRepository;

    public List<Model> getUserModels(User user){
        List<Model> models = modelRepository.findByUser(user);
        return models;
    }

    public List<Model> getGlobalModels(){
        return this.modelRepository.findByGlobal(true);
    }

    public User addUserModel(ModelDto modelDto, User user){
        Model model = new Model(modelDto.getName(), modelDto.getText(), modelDto.getProvider(), modelDto.getUrl(), modelDto.getType(), user);
        model.setGlobal(false);
        modelRepository.save(model);
        return userRepository.save(user);
    }

    public void deleteUserModel(Long id, User user) {
        Model model = this.getModel(id);
        if (model != null && model.getUser().equals(user)) {
            modelRepository.delete(model);

        } else {
            throw new SecurityException("to nie jest twój model.");
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
    public Model getModel(Long id) {
        return modelRepository.findById(id).orElse(null);
    }

}
