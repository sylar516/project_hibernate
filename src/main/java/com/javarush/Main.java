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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;

public class Main {
    private final SessionFactory sessionFactory;

    private ActorDAO actorDAO;
    private AddressDAO addressDAO;
    private CategoryDAO categoryDAO;
    private CityDAO cityDAO;
    private CountryDAO countryDAO;
    private CustomerDAO customerDAO;
    private FilmDAO filmDAO;
    private FilmTextDAO filmTextDAO;
    private InventoryDAO inventoryDAO;
    private LanguageDAO languageDAO;
    private PaymentDAO paymentDAO;
    private RentalDAO rentalDAO;
    private StaffDAO staffDAO;
    private StoreDAO storeDAO;

    public Main() {
        Properties properties = new Properties();
        properties.put(Environment.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
        properties.put(Environment.URL, "jdbc:p6spy:mysql://localhost:3306/movie");
        properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQLDialect");
        properties.put(Environment.USER, "root");
        properties.put(Environment.PASS, "350609sylarR");
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
//        Customer customer = main.createCustomer();
//        Session session = main.sessionFactory.getCurrentSession();
//        session.beginTransaction();
//        Customer customer = main.customerDAO.getById((short) 600);
//        Film film = main.filmDAO.getById((short) 6);
//        session.getTransaction().commit();
//        System.out.println(film.getSpecial_features());
//        System.out.println(film.getLanguage().getName());
//        System.out.println(film.getRating());
//        main.setFilmRating(film);
//        Set<Feature> specialFeatures = film.getSpecialFeatures();
//        System.out.println(specialFeatures);
//
//        main.setFilmSpecialFeatures(film);

//        System.out.println(customer.getAddress() + " " + customer.getFirstName() + " " + customer.getLastName());
//        main.customerReturnedInventoryToStore(customer.getId());

//        main.customerRentInventoryFromStore(customer, film);
//        main.releaseNewFilm();
    }

    private void releaseNewFilm() {
        try(Session session = sessionFactory.getCurrentSession()) {
            Random random = new Random();

            Transaction transaction = session.beginTransaction();
            Film film = new Film();
            film.setTitle("CATZILLA ATTACK PART 2");
            film.setDescription("A epic tale about catzilla again");
            film.setReleaseYear(Year.of(LocalDate.now().getYear()));

            Query<Long> queryLanguage = session.createQuery("select count(l) from Language l", Long.class);
            int languageCount = Math.toIntExact(queryLanguage.uniqueResult());
            byte languageId = (byte) random.nextInt(1,languageCount + 1);
            Language language = languageDAO.getById(languageId);
            film.setLanguage(language);

            film.setRentalDuration((byte) random.nextInt(1, 9));
            film.setRentalRate(new BigDecimal(random.nextDouble(0.99, 5)));
            film.setLength((short) 122);
            film.setReplacementCost(new BigDecimal(random.nextDouble(9.99, 30)));
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
        }
    }

    private void customerRentInventoryFromStore(Customer customer, Film film) {
        try(Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            Short filmId = film.getId();
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

            if (inventories.size() > rentals.size()) {
                Inventory inventory = inventories.get(0);
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
            } else { System.out.println("фильм " + film.getTitle() + " не доступен для аренды"); }
            transaction.commit();
        }
    }

    private void setFilmSpecialFeatures(Film film) {
        try(Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            Set<Feature> features = Set.of(Feature.DELETED_SCENES, Feature.BEHIND_THE_SCENES);
            film.setSpecialFeatures(features); // было Deleted Scenes,Behind the Scenes
            filmDAO.save(film);
            transaction.commit();
        }
    }

    private void setFilmRating(Film film) {
        try(Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            film.setRating(Rating.PG); //был рейтинг PG
            filmDAO.save(film);
            transaction.commit();
        }
    }

    private void customerReturnedInventoryToStore(Short customerId) {
        try(Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            Query<Rental> query = session.createQuery("select r from Rental r where r.customer.id = :CUSTOMER_ID and r.returnDate is null", Rental.class);
            query.setParameter("CUSTOMER_ID", customerId);
            query.setMaxResults(1);
            Rental rental = query.uniqueResult();
            if(rental != null) {
                rental.setReturnDate(LocalDateTime.now());
                rentalDAO.save(rental);
            }
            transaction.commit();
        }
    }

    private Customer createCustomer() {
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
            customer.setFirstName("Sonya");
            customer.setLastName("Koshkina");
            customer.setEmail("son@yandex.ru");
            customer.setAddress(address);
            customer.setActive(true);
            customerDAO.save(customer);

            transaction.commit();
            return customer;
        }
    }
}
