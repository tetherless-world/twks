package edu.rpi.tw.twks.api;

/**
 * Client for a TWKS server.
 */
public interface TwksClient extends AdministrationApi, AutoCloseable, AssertionQueryApi, GetAssertionsApi, NanopublicationCrudApi, NanopublicationQueryApi {
    @Override
    void close();

    TwksVersion getClientVersion();

    TwksVersion getServerVersion();
}
