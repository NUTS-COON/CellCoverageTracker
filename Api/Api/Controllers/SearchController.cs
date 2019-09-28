using Api.Models;
using Api.Services.Interfaces;
using Microsoft.AspNetCore.Cors;
using Microsoft.AspNetCore.Mvc;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Api.Logic;

namespace Api.Controllers
{
    [Route("api/[controller]/[action]")]
    [EnableCors("AllowAll")]
    [ApiController]
    public class SearchController : ControllerBase
    {
        private readonly IRouteSearcher _routeSearcher;
        private readonly IHereService _hereService;
        private readonly DataService _dataService;

        public SearchController(IRouteSearcher routeSearcher, IHereService hereService,
            DataService dataService)
        {
            _routeSearcher = routeSearcher;
            _hereService = hereService;
            _dataService = dataService;
        }

        [HttpPost]
        public async Task<List<TargetRoute>> FindRoutes([FromBody]RouteSearchInfo model)
        {
            return await _routeSearcher.GetHereRoutes(model.From, model.To, model.Time ?? DateTime.Now);
        }

        [HttpPost]
        public async Task<IEnumerable<SuggesionAddress>> GetSuggestionsWithCoordinates([FromBody]AddressText model)
        {
            return await _hereService.GetSuggestionsWithCoordinates(model?.Text);
        }

        [HttpPost]
        public async Task<IEnumerable<SuggesionAddress>> GetSuggestions([FromBody]AddressText model)
        {
            return await _hereService.GetSuggestions(model?.Text);
        }

        [HttpPost]
        public async Task<IEnumerable<SuggesionAddress>> GetPlacesSuggestions([FromBody]AddressText model)
        {
            return await _hereService.GetPlacesSuggestion(model?.Text);
        }

        [HttpPost]
        public async Task<Coordinate> GetCoordinate([FromBody]LocationIdModel model)
        {
            return await _hereService.GetCoordinate(model?.LocationId);
        }

        [HttpPost]
        public async Task<object> GetPoints([FromBody]RectangleOfSearch model)
        {
            return await _dataService.SearchGeo(new [] { model.LeftBottom, model.RightTop });
        }
    }
}