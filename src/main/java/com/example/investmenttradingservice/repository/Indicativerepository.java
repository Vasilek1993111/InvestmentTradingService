package com.example.investmenttradingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.investmenttradingservice.Entity.IndicativeEntity;

public interface Indicativerepository extends JpaRepository<IndicativeEntity, String> {

}
