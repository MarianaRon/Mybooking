import com.example.mybooking.model.Hotel;
import com.example.mybooking.model.User;
import com.example.mybooking.service.HotelService;
import com.example.mybooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/hotels")
public class HotelController {

    @Autowired
    private HotelService hotelService;

    @Autowired
    private UserService userService;

    @GetMapping("/hotel_list")
    public String getAllHotels(Model model) {
        model.addAttribute("hotels", hotelService.getAllHotels());
        model.addAttribute("users", userService.getAllUsers());
        return "hotel_list";
    }

    @PostMapping("/add")
    public String createHotel(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String address,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam Long owner,
            @RequestParam String housingType) {

        User ownerUser = userService.getUserById(owner)
                .orElseThrow(() -> new IllegalArgumentException("Invalid owner ID"));

        Hotel hotel = new Hotel(name, description, address, latitude, longitude, ownerUser, housingType);
        hotelService.saveHotel(hotel);

        return "redirect:/hotels/hotel_list";
    }

    @GetMapping("/edit/{id}")
    public String editHotel(@PathVariable Long id, Model model) {
        Hotel hotel = hotelService.getHotelById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));
        model.addAttribute("hotel", hotel);
        model.addAttribute("users", userService.getAllUsers());
        return "edit_hotel";
    }

    @PostMapping("/edit/{id}")
    public String updateHotel(@PathVariable Long id,
                              @RequestParam String name,
                              @RequestParam String description,
                              @RequestParam String address,
                              @RequestParam(required = false) Double latitude,
                              @RequestParam(required = false) Double longitude,
                              @RequestParam Long owner,
                              @RequestParam String housingType) {

        Hotel hotel = hotelService.getHotelById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        User ownerUser = userService.getUserById(owner)
                .orElseThrow(() -> new IllegalArgumentException("Invalid owner ID"));

        hotel.setName(name);
        hotel.setDescription(description);
        hotel.setAddress(address);
        hotel.setLatitude(latitude);
        hotel.setLongitude(longitude);
        hotel.setOwner(ownerUser);
        hotel.setHousingType(housingType);

        hotelService.saveHotel(hotel);

        return "redirect:/hotels/hotel_list";
    }

    @PostMapping("/delete/{id}")
    public String deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return "redirect:/hotels/hotel_list";
    }
}
