package com.mostafa.airbnbbackend.user.mapper;


import com.mostafa.airbnbbackend.user.dto.ReadUserDTO;
import com.mostafa.airbnbbackend.user.entity.Authority;
import com.mostafa.airbnbbackend.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    ReadUserDTO readUserDTOToUser(User user);

    default String mapAuthoritiesToString(Authority authority) {
        return authority.getName();
    }

}
