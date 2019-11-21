package edu.rpi.tw.twks.client;

import edu.rpi.tw.twks.api.AdministrationApi;
import edu.rpi.tw.twks.api.GetAssertionsApi;
import edu.rpi.tw.twks.api.NanopublicationCrudApi;
import edu.rpi.tw.twks.api.QueryApi;

/**
 * Client for a TWKS server.
 */
public interface TwksClient extends AdministrationApi, GetAssertionsApi, NanopublicationCrudApi, QueryApi {
}
