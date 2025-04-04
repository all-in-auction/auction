package com.auction.domain.user.entity;

import com.auction.common.entity.TimeStamped;
import com.auction.domain.auth.dto.request.SignupRequestDto;
import com.auction.domain.user.dto.request.UserUpdateRequestDto;
import com.auction.domain.user.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password")
    @NotNull
    private String password;

    @Column(name = "name")

    @NotNull
    private String name;

    @Column(name = "nick_name")
    private String nickName;

    @Column(name = "zip_code")
    private int zipCode;

    @Column(name = "address1")
    private String address1;

    @Column(name = "address2")
    private String address2;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "authority")
    private UserRole authority;

    @Column(name = "activate")
    @NotNull
    private boolean activate;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public User(String encodedPassword, SignupRequestDto requestDto) {
        this.email = requestDto.getEmail();
        this.password = encodedPassword;
        this.name = requestDto.getName();
        this.nickName = requestDto.getNickName();
        this.zipCode = requestDto.getZipCode();
        this.address1 = requestDto.getAddress1();
        this.address2 = requestDto.getAddress2();
        this.authority = UserRole.of(requestDto.getAuthority());
        this.activate = true;
    }

    public User(long id) {
        this.id = id;
    }

    public User(Long id, String email, String pw, String name, String nickName, int zipCode, String address1, String address2, String authority) {
        this.id = id;
        this.email = email;
        this.password = pw;
        this.name = name;
        this.nickName = nickName;
        this.zipCode = zipCode;
        this.address1 = address1;
        this.address2 = address2;
        this.authority = UserRole.valueOf(authority.toUpperCase());
    }

    public void changeDeactivate() {
        this.activate = false;
        this.deletedAt = LocalDateTime.now();
    }

    public static User fromUserId(long userId) {
        return new User(userId);
    }

    public void updateUser(UserUpdateRequestDto requestDto) {
        if (requestDto.getName() != null) this.name = requestDto.getName();
        if (requestDto.getNickName() != null) this.nickName = requestDto.getNickName();
        if (requestDto.getZipCode() != null) this.zipCode = Integer.parseInt(requestDto.getZipCode());
        if (requestDto.getAddress1() != null) this.address1 = requestDto.getAddress1();
        if (requestDto.getAddress2() != null) this.address2 = requestDto.getAddress2();
        if (requestDto.getAuthority() != null) this.authority = UserRole.of(requestDto.getAuthority());
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }
}