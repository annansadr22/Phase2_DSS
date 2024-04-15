package com.project.skiersendpoint;

import java.util.Random;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class skiersservlet {
   private final SkierRepository skierRepository;

   public skiersservlet(SkierRepository skierRepository) {
      this.skierRepository = skierRepository;
   }

   @PostMapping({"/skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skiersID}"})
   public ResponseEntity<String> doPOST(@PathVariable String resortID, @PathVariable String seasonID, @PathVariable String dayID, @PathVariable String skiersID, @RequestBody SkierRequest skierRequest) {
      if (!this.isValid(resortID, seasonID, dayID, skiersID)) {
         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid parameters supplied");
      } else if (this.shouldInjectException()) {
         int statusCode = this.getRandomStatusCode();
         return ResponseEntity.status(statusCode).body("Injected exception occurred");
      } else {
         int maxRetries = 5;
         int retryCount = 0;

         while(retryCount < maxRetries) {
            try {
               Skier skier = new Skier();
               skier.setDayID(dayID);
               skier.setResortID(resortID);
               skier.setSeasonID(seasonID);
               skier.setSkiersID(skiersID);
               skier.setTime(skierRequest.getTime());
               skier.setLiftID(skierRequest.getLiftID());
               this.skierRepository.save(skier);
               return ResponseEntity.status(HttpStatus.CREATED).body("Skier data saved to MongoDB.");
            } catch (Exception var9) {
               if (retryCount == maxRetries - 1) {
                  return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save skier data after multiple retries.");
               }

               ++retryCount;
            }
         }

         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error occurred.");
      }
   }
   
   private boolean shouldInjectException() {
	      int randomNumber = (new Random()).nextInt(100);
	      return randomNumber < 15;
	   }

	   private int getRandomStatusCode() {
	      int randomNumber = (new Random()).nextInt(2);
	      return randomNumber == 0 ? HttpStatus.BAD_REQUEST.value() : HttpStatus.INTERNAL_SERVER_ERROR.value();
	   }

   private boolean isValid(String resortID, String seasonID, String dayID, String skiersID) {
      try {
         int resortIdInt = Integer.parseInt(resortID);
         int seasonIdInt = Integer.parseInt(seasonID);
         int dayIdInt = Integer.parseInt(dayID);
         int skiersIdInt = Integer.parseInt(skiersID);
         return resortIdInt > 0 && seasonIdInt > 0 && dayIdInt > 0 && skiersIdInt > 0;
      } catch (NumberFormatException var9) {
         return false;
      }
   }

   
}