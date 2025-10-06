package com.spatialcollective.microtasktoolapi.repository

import com.spatialcollective.microtasktoolapi.model.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository :JpaRepository<UserEntity,String>{
}