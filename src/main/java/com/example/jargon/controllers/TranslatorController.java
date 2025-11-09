package com.example.jargon.controllers;



import com.example.jargon.service.JargonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
        import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/")
public class TranslatorController {


    @Autowired
    private JargonService jargonService;

    @GetMapping("/translate")
    public Map<String, String> translateToJargon(@RequestParam String sentence) {
        Map<String, String> response = new HashMap<>();
        response.put("original", sentence);

        try {
            // Call our service that talks to Claude API
            String jargonVersion = jargonService.translateToJargon(sentence);
            response.put("jargon", jargonVersion);
        } catch (Exception e) {
            response.put("jargon", "Error: " + e.getMessage());
        }

        return response;
    }

//    @GetMapping("/")
//    public String home() {
//        return "Meeting Jargon Translator is running! Try /translate?sentence=Let's meet tomorrow";
//    }
}

