package com.example.demo.repository;


import com.example.demo.model.GroupRead;
import com.example.demo.model.GroupRead.Id;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupReadRepository extends JpaRepository<GroupRead, Id> { }
