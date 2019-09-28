using Api.Models;
using Api.Settings;
using MongoDB.Driver;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace Api.Logic
{
    public class DataService
    {
        private readonly IMongoCollection<CellInfoMongoModel> _data;

        public DataService(MongoSettings settings)
        {
            var client = new MongoClient(settings.ConnectionString);
            var database = client.GetDatabase(settings.DatabaseName);

            _data = database.GetCollection<CellInfoMongoModel>(settings.CollectionName);
        }

        public async Task<long> CountByImei(string imei) => await _data.CountDocumentsAsync(x => x.IMEI == imei);

        public async Task Add(CellInfoSaveRequest item)
        {
            var data = new CellInfoMongoModel(item);
            await _data.InsertOneAsync(data);
        }
        

        public async Task Add(CellInfoSaveRequest[] items) => await _data.InsertManyAsync(items.Select(i => new CellInfoMongoModel(i)));

        public async Task<IEnumerable<CellPoint>> Get(RectangleOfSearch model)
        {
            var resultFilter = Builders<CellInfoMongoModel>.Filter.Empty;

            if (model.Filter?.Level != null)
                resultFilter &= Builders<CellInfoMongoModel>.Filter.Eq(c => c.Level, model.Filter.Level);

            if (!string.IsNullOrEmpty(model.Filter?.CellType))
                resultFilter &= Builders<CellInfoMongoModel>.Filter.Eq(c => c.CellType, model.Filter.CellType);

            if (!string.IsNullOrEmpty(model.Filter?.OperatorName))
                resultFilter &= Builders<CellInfoMongoModel>.Filter.Eq(c => c.OperatorName, model.Filter.OperatorName);

            if (model.LeftBottomCorner != null && model.RightTopCorner != null)
                resultFilter &= GetBetweenFilter(model.LeftBottomCorner, model.RightTopCorner);

            return (await _data.FindAsync(resultFilter))?.ToList()?.Select(c => new CellPoint(c));
        }

        private FilterDefinition<CellInfoMongoModel> GetBetweenFilter(Coordinate leftBottomCorner, Coordinate rightTopCorner)
        {
            return new FilterDefinitionBuilder<CellInfoMongoModel>().And(
                Builders<CellInfoMongoModel>.Filter.GeoWithinBox(c => c.Location, leftBottomCorner.Longitude, leftBottomCorner.Latitude, rightTopCorner.Longitude,rightTopCorner.Latitude));
        }
    }
}