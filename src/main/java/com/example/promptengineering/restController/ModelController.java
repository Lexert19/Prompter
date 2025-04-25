package com.example.promptengineering.restController;

import com.example.promptengineering.dto.ModelDto;
import com.example.promptengineering.entity.Model;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/models")
public class ModelController {
    @Autowired
    private ModelService modelService;

    @GetMapping("/user-models")
    public List<Model> getUserModels(@AuthenticationPrincipal User user) {
        return modelService.getUserModels(user);
    }
    @GetMapping("/global-models")
    public List<Model> getGlobalModels() {
        return modelService.getGlobalModels();
    }

    @PutMapping("/user-models/{id}")
    public ResponseEntity<String> editUserModel(@AuthenticationPrincipal User user, @PathVariable String id, @RequestBody ModelDto modelDto) {
        Model model = modelService.getModel(id);
        if (model != null && model.getUserId().equals(user.getId())) {
            modelService.editUserModel(model, modelDto);
            return ResponseEntity.ok("Zmieniono model!");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/user-models/{id}")
    public ResponseEntity<String> deleteUserModel(@AuthenticationPrincipal User user, @PathVariable String id) {
        try{
            modelService.deleteUserModel(id, user);
            return ResponseEntity.ok("Usunięto model");
        }catch (Exception e){
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/all-models")
    public List<Model> getAllModels(@AuthenticationPrincipal User user) {
        List<Model> userModels = modelService.getUserModels(user);
        List<Model> globalModels = modelService.getGlobalModels();
        Set<Model> allModels = new HashSet<>(userModels);
        allModels.addAll(globalModels);
        return new ArrayList<>(allModels);
    }

    @PostMapping("/user-models")
    public ResponseEntity<String> addUserModel(@AuthenticationPrincipal User user, @RequestBody ModelDto modelDto) {
        modelService.addUserModel(modelDto, user);
        return ResponseEntity.ok("Zapisano model!");
    }

}
