package edu.rpi.tw.twks.api;

/**
 * Client for a TWKS server.
 */
public interface TwksClient extends AdministrationApi, AssertionQueryApi, GetAssertionsApi, NanopublicationCrudApi, NanopublicationQueryApi {
    TwksVersion getClientVersion();

    TwksVersion getServerVersion();
}
