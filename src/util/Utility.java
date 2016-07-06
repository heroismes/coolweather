package util;

import android.R.integer;
import android.text.TextUtils;
import model.City;
import model.CoolWeatherDB;
import model.Country;
import model.Province;

public class Utility {

	//处理省级数据
	public synchronized static boolean handleProvincesResponse(String response,CoolWeatherDB db){
		if(!TextUtils.isEmpty(response)){
			String[] provinces = response.split(",");
			if (provinces != null && provinces.length > 0) {
				for(String p : provinces){
					String[] data = p.split("\\|");
					Province province = new Province();
					province.setProvinceName(data[1]);
					province.setProvinceCode(data[0]);
					//把数据加入到数据库中
					db.saveProvince(province);
				}
				return true;
			}
			
		}
		return false;
	}
	//保存城市数据
	public synchronized static boolean handleCitiesResponse(String response,int provinceId,CoolWeatherDB db){
		if(!TextUtils.isEmpty(response)){
			String[] allcities = response.split(",");
			if (allcities != null && allcities.length > 0) {
				for(String c:allcities){
					String[] array = c.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					db.saveCity(city);
				}
				
				return true;
			}
		}
		return false;
	}
	
	//保存县级数据
	public synchronized static boolean handleCountriesResponse(String response,int cityId,CoolWeatherDB db){
		if (!TextUtils.isEmpty(response)) {
			String[] allCountries = response.split(",");
			if (allCountries != null && allCountries.length > 0) {
				for(String c:allCountries){
					String[] array = c.split("\\|");
					Country country = new Country();
					country.setCountryCode(array[0]);
					country.setCountryName(array[1]);
					country.setCityId(cityId);
					
					db.saveCountry(country);
				}
				return true;
			}
		}
		return false;
	}
}
