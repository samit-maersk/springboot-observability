package net.samitkumar.springbootobservability.models;

import lombok.Builder;

import java.util.List;

public record Users(int id, String name, String username, String email, Users.Address address, String phone, String website,
             Users.Company company, List<Posts> posts, List<Albums> albums, List<Todos> todos) {
    @Builder
    public Users {
    }

    record Address(String street, String suite, String city, String zipcode, Users.Address.Geo geo) {
        @Builder
        public Address {
        }

        record Geo(String lat, String lng) {
            @Builder
            public Geo {
            }
        }
    }

    record Company(String name, String catchPhrase, String bs) {
        @Builder
        public Company {
        }
    }
}
