package com.rohitchouhan.classroom.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class CustomSQLExecutorService {

    @Autowired
    private DataSource dataSource;

    public void executeSQLQuery(String sqlQuery) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        // Process the results
                        // Example: String result = resultSet.getString("column_name");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
