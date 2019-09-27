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
    public class DataController : ControllerBase
    {
        private readonly DataService _dataService;
        
        public DataController(DataService dataService)
        {
            _dataService = dataService;
        }
        
        /// <summary>
        /// 
        /// </summary>
        /// <returns></returns>
        [HttpPost]
        public object Save([FromBody] CellInfo model)
        {
            _dataService.Add(model);
            return new { success = true };
        }
        
        [HttpPost]
        public long CountByImei([FromBody] CountByImeiRequest model)
        {
            return _dataService.CountByImei(model.Imei);
        }
    }
}