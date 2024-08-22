import com.example.mybooking.model.Hotel;
import com.example.mybooking.model.User;
import com.example.mybooking.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/hotels")
public class HotelController {

    @Autowired
    private HotelService hotelService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private AmenityService amenityService;

    @Autowired
    private ImageService imageService;

    // Отримання всіх готелів
    @GetMapping
    public String getAllHotels(Model model) {
        model.addAttribute("hotels", hotelService.getAllHotels());
        return "hotels";
    }

    // Отримання готелів, зареєстрованих партнером
    @GetMapping("/by-partner")
    public String getHotelsByPartner(@SessionAttribute("loggedInUser") User loggedInUser, Model model) {
        model.addAttribute("hotels", hotelService.getHotelsByOwner(loggedInUser));
        return "hotels_by_partner";
    }

    // Показ форми реєстрації готелю
    @GetMapping("/hotel_registration")
    public String showRegistrationForm(Model model) {
        model.addAttribute("hotel", new Hotel());
        model.addAttribute("users", userService.getAllUsers());
        return "hotel_form";
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

    // Отримання готелю за ID
    @GetMapping("/{id}")
    public String getHotelById(@PathVariable Long id, Model model) {
        return hotelService.getHotelById(id)
                .map(hotel -> {
                    model.addAttribute("hotel", hotel);
                    return "hotel_details";
                })
                .orElse("redirect:/hotels");
    }

    // Показ форми пошуку готелів
    @GetMapping("/search")
    public String showSearchForm(Model model) {
        model.addAttribute("searchTerm", "");
        return "search_form";
    }

    // Пошук готелів за назвою або описом
    @PostMapping("/search")
    public String searchHotels(@RequestParam String searchTerm, Model model) {
        List<Hotel> results = hotelService.searchHotelsByNameOrDescription(searchTerm);
        model.addAttribute("results", results);
        return "search_results";
    }
}
