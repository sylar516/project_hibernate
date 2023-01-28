package com.javarush.dao;

import com.javarush.domain.Rating;
import org.hibernate.SessionFactory;

public class RatingDAO extends GenericDAO<Rating>{
    public RatingDAO(SessionFactory sessionFactory) {
        super(Rating.class, sessionFactory);
    }
}
