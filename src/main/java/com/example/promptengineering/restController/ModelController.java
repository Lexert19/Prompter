package com.example.promptengineering.restController;

import com.example.promptengineering.dto.ModelDto;
import com.example.promptengineering.entity.Model;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/models")
public class ModelController {
    @Autowired
    private ModelService modelService;

    @GetMapping("/user-models")
    public List<ModelDto> getUserModels(@AuthenticationPrincipal User user) {
        List<Model> models = modelService.getUserModels(user);
        return ModelDto.toDtoList(models);
    }
    @GetMapping("/global-models")
    public List<ModelDto> getGlobalModels() {
        List<Model> models = modelService.getGlobalModels();
        return ModelDto.toDtoList(models);
    }

    @PutMapping("/user-models/{id}")
    public ResponseEntity<String> editUserModel(@AuthenticationPrincipal User user, @PathVariable Long id, @RequestBody ModelDto modelDto) {
        Optional<Model> model = modelService.getModel(id);
        if (model.isPresent() && model.get().getUser().equals(user)) {
            modelService.editUserModel(model.orElse(null), modelDto);
            return ResponseEntity.ok("Zmieniono model!");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/user-models/{id}")
    public ResponseEntity<String> deleteUserModel(@AuthenticationPrincipal User user, @PathVariable Long id) {
        try{
            modelService.deleteUserModel(id, user);
            return ResponseEntity.ok("Usunięto model");
        }catch (Exception e){
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/all-models")
    public List<ModelDto> getAllModels(@AuthenticationPrincipal User user) {
        List<Model> userModels = modelService.getUserModels(user);
        List<Model> globalModels = modelService.getGlobalModels();
        Set<Model> allModels = new HashSet<>(userModels);
        allModels.addAll(globalModels);
        return ModelDto.toDtoList(new ArrayList<>(allModels));
    }

    @PostMapping("/user-models")
    public ResponseEntity<String> addUserModel(@AuthenticationPrincipal User user, @RequestBody ModelDto modelDto) {
        modelService.addUserModel(modelDto, user);
        return ResponseEntity.ok("Zapisano model!");
    }

}
