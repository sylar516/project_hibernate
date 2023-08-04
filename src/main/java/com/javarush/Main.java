package com.javarush;

import com.javarush.dao.*;
import com.javarush.domain.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;

public class Main {
    private final SessionFactory sessionFactory;

    private final ActorDAO actorDAO;
    private final AddressDAO addressDAO;
    private final CategoryDAO categoryDAO;
    private final CityDAO cityDAO;
    private final CountryDAO countryDAO;
    private final CustomerDAO customerDAO;
    private final FilmDAO filmDAO;
    private final FilmTextDAO filmTextDAO;
    private final InventoryDAO inventoryDAO;
    private final LanguageDAO languageDAO;
    private final PaymentDAO paymentDAO;
    private final RentalDAO rentalDAO;
    private final StaffDAO staffDAO;
    private final StoreDAO storeDAO;

    public Main() {
        Properties properties = new Properties();
        properties.put(Environment.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
        properties.put(Environment.URL, "jdbc:p6spy:mysql://localhost:3306/movie");
        properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQLDialect");
        properties.put(Environment.USER, "root");
        properties.put(Environment.PASS, "root");
        properties.put(Environment.SHOW_SQL, "true");
        properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread"); //настройка нужна для корректной работы кэша при многопоточности
        properties.put(Environment.HBM2DDL_AUTO, "validate");

        sessionFactory = new Configuration()
                .setProperties(properties)
                .addAnnotatedClass(Actor.class)
                .addAnnotatedClass(Address.class)
                .addAnnotatedClass(Category.class)
                .addAnnotatedClass(City.class)
                .addAnnotatedClass(Country.class)
                .addAnnotatedClass(Customer.class)
                .addAnnotatedClass(Film.class)
                .addAnnotatedClass(FilmText.class)
                .addAnnotatedClass(Inventory.class)
                .addAnnotatedClass(Language.class)
                .addAnnotatedClass(Payment.class)
                .addAnnotatedClass(Rental.class)
                .addAnnotatedClass(Staff.class)
                .addAnnotatedClass(Store.class)
                .buildSessionFactory();

        actorDAO = new ActorDAO(sessionFactory);
        addressDAO = new AddressDAO(sessionFactory);
        categoryDAO = new CategoryDAO(sessionFactory);
        cityDAO = new CityDAO(sessionFactory);
        countryDAO = new CountryDAO(sessionFactory);
        customerDAO = new CustomerDAO(sessionFactory);
        filmDAO = new FilmDAO(sessionFactory);
        filmTextDAO = new FilmTextDAO(sessionFactory);
        inventoryDAO = new InventoryDAO(sessionFactory);
        languageDAO = new LanguageDAO(sessionFactory);
        paymentDAO = new PaymentDAO(sessionFactory);
        rentalDAO = new RentalDAO(sessionFactory);
        staffDAO = new StaffDAO(sessionFactory);
        storeDAO = new StoreDAO(sessionFactory);
    }

    public static void main(String[] args) {
        Main main = new Main();

        Scanner scanner = new Scanner(System.in);
        System.out.print("""
                События:
                1. добавление нового клиента проката в базу данных
                2. клиент пошел и вернул ранее арендованный фильм
                3. клиент сходил в магазин (store) и арендовал (rental) там инвентарь (inventory). При этом он сделал оплату (payment) у продавца (staff)
                4. сняли новый фильм, и он стал доступен для аренды
                
                Выберите событие и введите его номер:
                """);
        int eventNumber = scanner.nextInt();

        switch (eventNumber) {
            case 1 -> main.createCustomer();
            case 2 -> main.customerReturnedInventoryToStore();
            case 3 -> main.customerRentInventoryFromStore();
            case 4 -> main.releaseNewFilm();
            default -> System.out.println("события с таким номером не существует");
        }
    }

    private void createCustomer() {
        try(Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            Byte storeId = (byte) new Random().nextInt(1, 3);
            Store store = storeDAO.getById(storeId);

            City city = cityDAO.getByName("La Romana");

            Address address = new Address();
            address.setAddress("Tverskaya, 10");
            address.setCity(city);
            address.setDistrict("Russia");
            address.setPhone("89039761232");
            address.setPostalCode("451300");
            addressDAO.save(address);

            Customer customer = new Customer();
            customer.setStore(store);
            customer.setFirstName("SONYA");
            customer.setLastName("KOSHKINA");
            customer.setEmail("sonya@yandex.ru");
            customer.setAddress(address);
            customer.setActive(true);
            customerDAO.save(customer);

            transaction.commit();

            System.out.printf("\nОтлично, клиент %s %s добавлен в базу данных проката", customer.getFirstName(), customer.getLastName());
        }
    }

    private void customerReturnedInventoryToStore() {
        try(Session session = sessionFactory.getCurrentSession()) {
            Random random = new Random();
            Transaction transaction = session.beginTransaction();

            Query<Rental> rentalQuery = session.createQuery("select r from Rental r where r.returnDate is null", Rental.class);
            List<Rental> resultList = rentalQuery.getResultList();
            Rental rental = resultList.get(random.nextInt(resultList.size()));

            rental.setReturnDate(LocalDateTime.now());
            rentalDAO.save(rental);

            Customer customer = rental.getCustomer();
            Film film = rental.getInventory().getFilm();
            Inventory inventory = rental.getInventory();

            transaction.commit();

            System.out.printf("\nОтлично, клиент %s (id = %d) вернул кассету (id = %d) c фильмом %s (id = %d), событие аренды c id = %d закрыто",
                    customer.getFirstName(), customer.getId(), inventory.getId(), film.getTitle(), film.getId(), rental.getId());
        }
    }

    private void customerRentInventoryFromStore() {
        try(Session session = sessionFactory.getCurrentSession()) {
            Random random = new Random();

            Transaction transaction = session.beginTransaction();

            Query<Long> customerQuery = session.createQuery("select count(c) from Customer c", Long.class);
            int customerCount = Math.toIntExact(customerQuery.getSingleResult());
            short customerId = (short) random.nextInt(customerCount + 1);
            Customer customer = customerDAO.getById(customerId);

            Query<Long> filmQuery = session.createQuery("select count(f) from Film f", Long.class);
            int filmCount = Math.toIntExact(filmQuery.getSingleResult());
            short filmId = (short) random.nextInt(filmCount + 1);
            Film film = filmDAO.getById(filmId);

            Byte storeID = customer.getStore().getId();

            Query<Inventory> inventoryQuery = session.createQuery("select inv from Inventory inv where inv.film.id = :FILM_ID and inv.store.id = :STORE_ID", Inventory.class);
            inventoryQuery.setParameter("FILM_ID", filmId);
            inventoryQuery.setParameter("STORE_ID", storeID);
            List<Inventory> inventories = inventoryQuery.list();

            Query<Rental> rentalQuery = session.createQuery("select r from Rental r where r.inventory.film.id = :FILM_ID " +
                    "and r.inventory.store.id = :STORE_ID and r.returnDate is null", Rental.class);
            rentalQuery.setParameter("FILM_ID", filmId);
            rentalQuery.setParameter("STORE_ID", storeID);
            List<Rental> rentals = rentalQuery.list();

            Inventory inventory;

            if (inventories.size() > rentals.size()) {
                inventory = inventories.get(0);
                Store store = storeDAO.getById(storeID);
                Staff staff = store.getManagerStaff();

                Rental rental = new Rental();
                rental.setRentalDate(LocalDateTime.now());
                rental.setInventory(inventory);
                rental.setCustomer(customer);
                rental.setStaff(staff);
                rentalDAO.save(rental);

                Payment payment = new Payment();
                payment.setAmount(new BigDecimal("1.99"));
                payment.setCustomer(customer);
                payment.setStaff(staff);
                payment.setRental(rental);
                paymentDAO.save(payment);
            } else {
                System.out.println("фильм " + film.getTitle() + " не доступен для аренды");
                return;
            }

            Query<Long> query = session.createQuery("select count(r) from Rental r", Long.class);
            Long lastRentalId = query.getSingleResult();

            transaction.commit();

            System.out.printf("\nОтлично, клиент %s (id = %d) арендовал для просмотра кассету (id = %d) с фильмом %s (id = %d), создано событие аренды с id = %d",
                    customer.getFirstName(), customer.getId(), inventory.getId(), film.getTitle(), film.getId(), lastRentalId);
        }
    }

    private void releaseNewFilm() {
        try(Session session = sessionFactory.getCurrentSession()) {
            Random random = new Random();

            Transaction transaction = session.beginTransaction();
            Film film = new Film();
            film.setTitle("CATZILLA ATTACK PART 3");
            film.setDescription("A epic tale about catzilla again");
            film.setReleaseYear(Year.now());

            Query<Long> queryLanguage = session.createQuery("select count(l) from Language l", Long.class);
            int languageCount = Math.toIntExact(queryLanguage.uniqueResult());
            byte languageId = (byte) random.nextInt(1,languageCount + 1);
            Language language = languageDAO.getById(languageId);
            film.setLanguage(language);

            film.setRentalDuration((byte) random.nextInt(1, 9));
            film.setRentalRate(BigDecimal.valueOf(random.nextDouble(0.99, 5)));
            film.setLength((short) 122);
            film.setReplacementCost(BigDecimal.valueOf(random.nextDouble(9.99, 30)));
            film.setRating(Rating.NC17);

            Set<Feature> features = Set.of(Feature.TRAILERS, Feature.COMMENTARIES);
            film.setSpecialFeatures(features);

            Set<Actor> actors = new HashSet<>();
            Query<Long> queryActor = session.createQuery("select count(a) from Actor a", Long.class);
            int actorCount = Math.toIntExact(queryActor.uniqueResult());
            for (int i = 0; i < 20; i++) {
                short actorId = (short) random.nextInt(1, actorCount + 1);
                Actor actor = actorDAO.getById(actorId);
                actors.add(actor);
            }
            film.setActors(actors);

            Set<Category> categories = new HashSet<>();
            Query<Long> queryCategory = session.createQuery("select count(c) from Category c", Long.class);
            int categoryCount = Math.toIntExact(queryCategory.uniqueResult());
            for (int i = 0; i < 2; i++) {
                byte categoryId = (byte) random.nextInt(1, categoryCount + 1);
                Category category = categoryDAO.getById(categoryId);
                categories.add(category);
            }
            film.setCategories(categories);

            filmDAO.save(film);

            int filmCopies = random.nextInt(1,11);
            for (int i = 0; i < filmCopies; i++) {
                Inventory inventory = new Inventory();
                inventory.setFilm(film);

                Store store = storeDAO.getById((byte)random.nextInt(1,3));
                inventory.setStore(store);

                inventoryDAO.save(inventory);
            }

            transaction.commit();

            System.out.printf("\nОтлично, теперь все желающие смогут увидеть фильм \"%s\"!!!", film.getTitle());
        }
    }
}
