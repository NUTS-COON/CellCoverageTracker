using Api.Logic;
using Api.Models;
using Microsoft.AspNetCore.Mvc;

namespace Api.Controllers
{
    /// <summary>
    /// 
    /// </summary>
    [Route("api/[controller]/[action]")]
    [ApiController]
    public class SearchController : ControllerBase
    {
        /// <summary>
        /// 
        /// </summary>
        /// <returns></returns>
        [HttpGet]
        public SuggestionResponse[] Suggestion(string name)
        {
            return new []
            { 
                new SuggestionResponse
                {
                    Name = "Казань",
                    StationId = 2060615
                },
                new SuggestionResponse
                {
                    Name = "Аэропорт Казань",
                    StationId = 2060790
                }
            };
        }
        
        /// <summary>
        /// 
        /// </summary>
        /// <returns></returns>
        [HttpGet]
        public FindRouteResponse[] FindRoutes(int stationFromId, int stationToId)
        {
            return new []
            { 
                new FindRouteResponse
                {
                    Name = "Казань - Аэропорт Казань",
                    Id = "fake1"
                }
            };
        }
        
        /// <summary>
        /// 
        /// </summary>
        /// <returns></returns>
        [HttpGet]
        public RouteLocationInfo[] GetRoute(string routeId)
        {
            return new []
            { 
                new RouteLocationInfo
                {
                    Latitude = 1,
                    Longitude = 1
                }
            };
        }
    }
}